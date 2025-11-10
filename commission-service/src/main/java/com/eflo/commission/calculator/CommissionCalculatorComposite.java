package com.eflo.commission.calculator;

import com.eflo.commission.domain.enums.CalculationMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@Primary
@Slf4j
public class CommissionCalculatorComposite implements CommissionCalculator {

    private final Map<CalculationMethod, CommissionCalculator> calculators;

    public CommissionCalculatorComposite(List<CommissionCalculator> calculatorList) {
        this.calculators = new EnumMap<>(CalculationMethod.class);

        for (CommissionCalculator calculator : calculatorList) {
            // Skip self to avoid circular dependency
            if (calculator instanceof CommissionCalculatorComposite) {
                continue;
            }

            // Map each calculator using its getCalculationMethod()
            CalculationMethod method = calculator.getCalculationMethod();
            calculators.put(method, calculator);
            log.debug("Registered calculator {} for method {}", calculator.getClass().getSimpleName(), method);
        }

        log.info("Initialized CommissionCalculatorComposite with {} calculators", calculators.size());
    }

    @Override
    public CommissionCalculationResult calculate(CommissionCalculationContext context) {
        CalculationMethod method = context.getCommissionScale().getCalculationMethod();

        CommissionCalculator calculator = calculators.get(method);
        if (calculator == null) {
            throw new IllegalArgumentException("No calculator found for calculation method: " + method);
        }

        log.debug("Using calculator {} for method {}", calculator.getClass().getSimpleName(), method);
        return calculator.calculate(context);
    }

    @Override
    public boolean supports(CalculationMethod method) {
        return calculators.containsKey(method);
    }

    @Override
    public CalculationMethod getCalculationMethod() {
        // Composite supports all methods
        return null;
    }
}
