package com.eflo.order.service;

import com.eflo.order.domain.entity.*;
import com.eflo.order.domain.model.dto.*;
import com.eflo.order.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoveImportService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderCommercialActionRepository commercialActionRepository;
    private final OrderTradeInRepository tradeInRepository;
    private final OrderOptionRepository optionRepository;
    private final OrderSupplementRepository supplementRepository;
    private final OrderContractServiceRepository contractServiceRepository;
    private final ConditionEvaluationService conditionEvaluationService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public Order importMoveOrder(MoveOrderRequest request, Long createdByUserId) {
        log.info("Starting MOVE order import for contract: {}", request.getPrix().getNumContratCommande());

        MovePrixData prix = request.getPrix();

        // 1. Create or find customer
        Customer customer = createOrFindCustomer(prix);

        // 2. Create Order entity
        Order order = buildOrder(prix, customer, createdByUserId);

        // 3. Save order first to get ID
        order = orderRepository.save(order);

        // 4. Create commercial actions
        if (request.getActionCo() != null && !request.getActionCo().isEmpty()) {
            for (MoveActionCoData actionData : request.getActionCo()) {
                OrderCommercialAction action = buildCommercialAction(order, actionData);
                commercialActionRepository.save(action);
            }
        }

        // 5. Create trade-in if present
        if (hasTradeIn(prix)) {
            OrderTradeIn tradeIn = buildTradeIn(order, prix);
            tradeInRepository.save(tradeIn);
            order.setHasTradeIn(true);
        }

        // 6. Create options
        if (request.getOption() != null && !request.getOption().isEmpty()) {
            for (MoveOptionData optionData : request.getOption()) {
                OrderOption option = buildOption(order, optionData, createdByUserId);
                optionRepository.save(option);
            }
        }

        // 7. Create supplements
        if (request.getSupplement() != null && !request.getSupplement().isEmpty()) {
            for (MoveSupplementData suppData : request.getSupplement()) {
                OrderSupplement supplement = buildSupplement(order, suppData, createdByUserId);
                supplementRepository.save(supplement);
            }
        }

        // 8. Create services
        if (request.getService() != null && !request.getService().isEmpty()) {
            for (MoveServiceData serviceData : request.getService()) {
                OrderContractService service = buildService(order, serviceData, createdByUserId);
                contractServiceRepository.save(service);
            }
        }

        // 9. Calculate totals
        recalculateOrderTotals(order);

        // 10. Save updated order
        order = orderRepository.save(order);

        log.info("Successfully imported MOVE order with ID: {}", order.getId());

        // 11. Publish order.created event (Kafka listener will auto-assign conditions)
        // Note: Event publishing happens in the calling controller or via Spring Events
        // Conditions will be assigned asynchronously via Kafka listener

        return order;
    }

    private Customer createOrFindCustomer(MovePrixData prix) {
        String email = prix.getEmailClient();

        // Try to find existing customer by email
        if (StringUtils.hasText(email)) {
            Optional<Customer> existing = customerRepository.findByEmail(email);
            if (existing.isPresent()) {
                log.info("Found existing customer with email: {}", email);
                return existing.get();
            }
        }

        // Create new customer
        log.info("Creating new customer: {} {}", prix.getPrenomClient(), prix.getNomClient());
        Customer customer = Customer.builder()
                .customerType(prix.getTypeClient()) // PA or PRO
                .civility(prix.getCiviliteClient())
                .firstName(prix.getPrenomClient())
                .lastName(prix.getNomClient())
                .commercialName(prix.getClientFinal())
                .email(prix.getEmailClient())
                .phoneMobile(prix.getTelClientPortable())
                .phoneLandline(prix.getTelClientFixe())
                .address(prix.getAdresseClient())
                .postalCode(prix.getCodePostalClient())
                .city(prix.getVilleClient())
                .country("FR")
                .sa(prix.getSaClient())
                .isActive(true)
                .build();

        return customerRepository.save(customer);
    }

    private Order buildOrder(MovePrixData prix, Customer customer, Long createdByUserId) {
        return Order.builder()
                // Order identification
                .orderNumber(prix.getNumContratCommande())
                .orderType(mapOrderType(prix.getGenre()))
                .status(Order.OrderStatus.DRAFT)

                // Customer
                .customerId(customer.getId())
                .customerFirstName(customer.getFirstName())
                .customerLastName(customer.getLastName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhoneMobile())
                .customerAddress(customer.getAddress())
                .customerPostalCode(customer.getPostalCode())
                .customerCity(customer.getCity())
                .customerCivility(customer.getCivility())
                .customerType(customer.getCustomerType())
                .customerSa(customer.getSa())
                .customerCommercialName(customer.getCommercialName())

                // Business organization
                .businessUnitId(1L) // Default, should be mapped properly
                .establishmentName(prix.getNomEtablissement())
                .identifiantRr(prix.getIdentifiantRr())
                .rattachement(prix.getRattachement())

                // Salesperson
                .salespersonId(1L) // Default, should be looked up by IPN
                .salespersonIpn(prix.getUserIpn())
                .salespersonName(buildSalespersonName(prix))
                .sellerType(prix.getTypeVendeur())

                // Vehicle information
                .make(prix.getMarque())
                .model(prix.getModele())
                .trim(prix.getVersion())
                .semiClairModel(prix.getSemiClairModele())
                .semiClairVersion(prix.getSemiClairVersion())
                .colorExterior(prix.getCouleur())
                .fuelType(prix.getEnergie())
                .co2Level(prix.getNivCo2())
                .bodyType(prix.getGamme())

                // Business metadata
                .productType(prix.getTypeProduit())
                .tariffNumber(prix.getNumTarif())
                .barcode(prix.getCodeBareme())
                .familyBarcode(prix.getFamilleBareme())
                .distrinetCode(prix.getCodeDistrinet())
                .distrinetExportNumber(prix.getNumExport())

                // Pricing
                .basePrice(defaultIfNull(prix.getPrixHtPublic()))
                .costPrice(defaultIfNull(prix.getPrixHtConcessionnaire()))
                .discountAmount(defaultIfNull(prix.getMontantRemise()))
                .vatRate(convertVatRate(prix.getTauxTva()))

                // Financing
                .financingType(mapFinancingType(prix.getCategorieFinanDms()))
                .financingContractDiac(prix.getNumContratFinanDiac())
                .financingAmount(defaultIfNull(prix.getMontantAFinancer()))
                .financingWithDeposit(toBoolean(prix.getFinancementAvecCaution()))
                .financingNumberOfServices(prix.getNbreAssurances())

                // Aids
                .aideRpe(defaultIfNull(prix.getAideRpe()))
                .aideAutres(defaultIfNull(prix.getAideAutres()))

                // Dates
                .expectedDeliveryDate(parseDate(prix.getDateLivraison()))
                .createdAt(parseDateTime(prix.getDateCreate()))

                // Audit
                .createdByUserId(createdByUserId)
                .build();
    }

    private OrderCommercialAction buildCommercialAction(Order order, MoveActionCoData data) {
        return OrderCommercialAction.builder()
                .order(order)
                .actionCode(data.getLibelle())
                .actionLabel(data.getLibelle())
                .amountOrPercentage(defaultIfNull(data.getMontantPourcentage()))
                .isPercentage(toBoolean(data.getPourcentage()))
                .vatRate(convertVatRate(data.getTauxTva()))
                .vatType(data.getTypeTva())
                .build();
    }

    private OrderTradeIn buildTradeIn(Order order, MovePrixData prix) {
        return OrderTradeIn.builder()
                .order(order)
                .ownerName(prix.getRepVoNomCg())
                .vehicleType("VP")
                .brand(prix.getRepVoMarque())
                .model(prix.getRepVoModele())
                .vin(prix.getRepVoChassis())
                .fuelType(prix.getRepVoEnergie())
                .mileage(prix.getRepVoKm())
                .origin(prix.getRepVoOrigine())
                .registrationDate(parseDate(prix.getRepVoImmaDate()))
                .firstRegistrationDate(parseDate(prix.getRepVoImmaDate1()))
                .conversionBonus(defaultIfNull(prix.getRepVoPrimeConv()))
                .engagementAmount(defaultIfNull(prix.getMontantEngagementReprise()))
                .overestimation(defaultIfNull(prix.getSurestimationRepVo()))
                .finalValue(defaultIfNull(prix.getValeurRepriseVo()))
                .status("EVALUATED")
                .build();
    }

    private OrderOption buildOption(Order order, MoveOptionData data, Long createdByUserId) {
        BigDecimal price = defaultIfNull(data.getMontant());
        BigDecimal discount = defaultIfNull(data.getMontantRemise());

        return OrderOption.builder()
                .order(order)
                .optionCode(data.getSemiClair())
                .optionName(data.getLibelle())
                .price(price.subtract(discount))
                .cost(BigDecimal.ZERO)
                .isMandatory(false)
                .isFactoryOption(true)
                .createdByUserId(createdByUserId)
                .build();
    }

    private OrderSupplement buildSupplement(Order order, MoveSupplementData data, Long createdByUserId) {
        return OrderSupplement.builder()
                .order(order)
                .supplementCode(generateSupplementCode(data.getLibelle()))
                .supplementName(data.getLibelle())
                .supplementType(mapSupplementType(data.getLibelle()))
                .amount(defaultIfNull(data.getMontant()))
                .createdByUserId(createdByUserId)
                .build();
    }

    private OrderContractService buildService(Order order, MoveServiceData data, Long createdByUserId) {
        BigDecimal price = defaultIfNull(data.getMontantServices());
        BigDecimal discount = defaultIfNull(data.getMontantRemiseServices());

        return OrderContractService.builder()
                .order(order)
                .serviceCode(data.getCodeBaremeServices())
                .serviceName(data.getLibelleServices())
                .serviceType(mapServiceType(data.getTypeServices()))
                .price(price.subtract(discount))
                .cost(BigDecimal.ZERO)
                .durationMonths(data.getDureeServices())
                .createdByUserId(createdByUserId)
                .build();
    }

    private void recalculateOrderTotals(Order order) {
        // Calculate options total
        BigDecimal optionsTotal = order.getOptions().stream()
                .map(OrderOption::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setOptionsTotal(optionsTotal);

        // Calculate supplements total
        BigDecimal supplementsTotal = order.getSupplements().stream()
                .map(OrderSupplement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSupplementsTotal(supplementsTotal);

        // Calculate services total
        BigDecimal servicesTotal = order.getContractServices().stream()
                .map(OrderContractService::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setServicesTotal(servicesTotal);

        // Calculate subtotal
        BigDecimal subtotal = order.getBasePrice()
                .add(optionsTotal)
                .add(supplementsTotal)
                .add(servicesTotal);
        order.setSubtotal(subtotal);

        // Calculate total before tax
        BigDecimal totalBeforeTax = subtotal
                .subtract(order.getDiscountAmount())
                .subtract(order.getAideRpe())
                .subtract(order.getAideAutres());
        order.setTotalBeforeTax(totalBeforeTax);

        // Calculate VAT
        BigDecimal vatRate = order.getVatRate().divide(BigDecimal.valueOf(100));
        BigDecimal vatAmount = totalBeforeTax.multiply(vatRate);
        order.setVatAmount(vatAmount);

        // Calculate total amount
        BigDecimal totalAmount = totalBeforeTax.add(vatAmount);
        order.setTotalAmount(totalAmount);

        // Calculate margins
        BigDecimal grossMargin = order.getBasePrice().subtract(order.getCostPrice());
        order.setGrossMargin(grossMargin);
        order.setNetMargin(grossMargin.subtract(order.getDiscountAmount()));

        if (order.getBasePrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal marginPercentage = order.getNetMargin()
                    .divide(order.getBasePrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            order.setMarginPercentage(marginPercentage);
        }
    }

    private boolean hasTradeIn(MovePrixData prix) {
        return StringUtils.hasText(prix.getRepVoChassis()) ||
               StringUtils.hasText(prix.getRepVoMarque());
    }

    private Order.OrderType mapOrderType(String genre) {
        if (genre == null) return Order.OrderType.VN;
        switch (genre.toUpperCase()) {
            case "VN": return Order.OrderType.VN;
            case "VO": return Order.OrderType.VO;
            case "EVO": return Order.OrderType.EVO;
            default: return Order.OrderType.VN;
        }
    }

    private String mapFinancingType(String categorieFinanDms) {
        if (!StringUtils.hasText(categorieFinanDms)) return "CASH";
        switch (categorieFinanDms.toUpperCase()) {
            case "LOA": return "LOA";
            case "LLD": return "LLD";
            case "CREDIT": return "LOAN";
            default: return "CASH";
        }
    }

    private String mapServiceType(String typeServices) {
        if (!StringUtils.hasText(typeServices)) return "OTHER";
        switch (typeServices.toUpperCase()) {
            case "PA": return "MAINTENANCE_PACK";
            case "GA": return "WARRANTY";
            case "AS": return "ASSISTANCE";
            default: return "OTHER";
        }
    }

    private String mapSupplementType(String libelle) {
        if (!StringUtils.hasText(libelle)) return "OTHER";
        String upper = libelle.toUpperCase();
        if (upper.contains("ADMIN") || upper.contains("MISE EN MAIN")) return "ADMIN_FEES";
        if (upper.contains("CARBURANT")) return "FUEL";
        if (upper.contains("TAPIS") || upper.contains("ACC")) return "ACCESSORIES";
        if (upper.contains("TAXE")) return "TAX";
        if (upper.contains("IMMATRICULATION") || upper.contains("CERTIFICAT")) return "REGISTRATION";
        if (upper.contains("ENVOI") || upper.contains("LIVRAISON")) return "DELIVERY";
        return "OTHER";
    }

    private String generateSupplementCode(String libelle) {
        if (!StringUtils.hasText(libelle)) return "SUPP";
        String cleaned = libelle.replaceAll("[^A-Z0-9]", "");
        if (cleaned.isEmpty()) return "SUPP";
        return cleaned.substring(0, Math.min(10, cleaned.length()));
    }

    private String buildSalespersonName(MovePrixData prix) {
        if (StringUtils.hasText(prix.getPrenomVendeur()) && StringUtils.hasText(prix.getNomVendeur())) {
            return prix.getPrenomVendeur() + " " + prix.getNomVendeur();
        }
        return prix.getUserIpn();
    }

    private BigDecimal convertVatRate(BigDecimal tauxTva) {
        if (tauxTva == null) return BigDecimal.valueOf(20.00);
        // Convert 0.2 to 20.00
        return tauxTva.multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal defaultIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Boolean toBoolean(Integer value) {
        return value != null && value > 0;
    }

    private LocalDate parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) return null;
        try {
            // Try full datetime format first
            return LocalDateTime.parse(dateStr, DATE_FORMATTER).toLocalDate();
        } catch (DateTimeParseException e) {
            try {
                // Try date only format
                return LocalDate.parse(dateStr, DATE_ONLY_FORMATTER);
            } catch (DateTimeParseException ex) {
                log.warn("Failed to parse date: {}", dateStr);
                return null;
            }
        }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (!StringUtils.hasText(dateStr)) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse datetime: {}", dateStr);
            return LocalDateTime.now();
        }
    }
}
