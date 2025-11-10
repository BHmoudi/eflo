/**
 * Script to create complete VN (Vehicle Neuf - New Vehicle) Workflow
 * Run with: npx tsx scripts/create-vn-workflow.ts
 */

const WORKFLOW_SERVICE_URL = 'http://localhost:8080/workflow-service/api/v1';
const WORKFLOW_CODE = 'WF_VN_COMPLETE';

interface WorkflowState {
  code: string;
  name: string;
  description: string;
  order: number;
  isInitial: boolean;
  isFinal: boolean;
  slaHours?: number;
  slaDays?: number;
}

interface WorkflowTask {
  code: string;
  name: string;
  description: string;
  taskType: 'DOCUMENT' | 'ACTION' | 'CONTROL' | 'APPROVAL';
  order: number;
  mandatory: boolean;
  requiresApproval: boolean;
  roleCode: string;
  slaHours?: number;
  dependencies?: string[];
  requiredDocuments?: string[];
}

interface WorkflowTransition {
  name: string;
  fromStateCode: string;
  toStateCode: string;
  trigger?: string;
}

const VN_WORKFLOW_SPECIFICATION = {
  workflowCode: WORKFLOW_CODE,
  workflowName: 'Complete VN Order Workflow',
  description: 'Complete workflow for new vehicle orders with 7 states and comprehensive task/document management',

  states: [
    {
      code: 'DRAFT',
      name: 'Draft',
      description: 'Initial state - Order is being created and drafted',
      order: 1,
      isInitial: true,
      isFinal: false,
      tasks: []
    },
    {
      code: 'PENDING',
      name: 'Pending Verification',
      description: 'Customer verification and document collection',
      order: 2,
      isInitial: false,
      isFinal: false,
      slaHours: 24,
      tasks: [
        {
          code: 'VERIFY_CUSTOMER',
          name: 'Verify Customer Information',
          description: 'Verify customer ID, address, and contact information',
          taskType: 'CONTROL' as const,
          order: 1,
          mandatory: true,
          requiresApproval: false,
          roleCode: 'SALES_ADMIN'
        },
        {
          code: 'UPLOAD_CUSTOMER_ID',
          name: 'Upload Customer ID Document',
          description: 'Upload proof of identity (ID card or passport)',
          taskType: 'DOCUMENT' as const,
          order: 2,
          mandatory: true,
          requiresApproval: false,
          roleCode: 'SALES_PERSON',
          requiredDocuments: ['CUSTOMER_ID']
        },
        {
          code: 'UPLOAD_PROOF_ADDRESS',
          name: 'Upload Proof of Address',
          description: 'Upload utility bill or bank statement (< 3 months old)',
          taskType: 'DOCUMENT' as const,
          order: 3,
          mandatory: true,
          requiresApproval: false,
          roleCode: 'SALES_PERSON',
          requiredDocuments: ['PROOF_ADDRESS']
        }
      ]
    },
    {
      code: 'CONFIRMED',
      name: 'Order Confirmed',
      description: 'Contract generation, signing, and payment processing',
      order: 3,
      isInitial: false,
      isFinal: false,
      slaHours: 48,
      tasks: [
        {
          code: 'GENERATE_CONTRACT',
          name: 'Generate Purchase Contract',
          description: 'Auto-generate purchase contract from order data',
          taskType: 'ACTION' as const,
          order: 1,
          mandatory: true,
          requiresApproval: false,
          roleCode: 'CONTRACT_ADMIN'
        },
        {
          code: 'UPLOAD_SIGNED_CONTRACT',
          name: 'Upload Signed Contract',
          description: 'Upload contract signed by customer',
          taskType: 'DOCUMENT' as const,
          order: 2,
          mandatory: true,
          requiresApproval: true,
          roleCode: 'SALES_PERSON',
          requiredDocuments: ['SIGNED_CONTRACT']
        },
        {
          code: 'VERIFY_DEPOSIT',
          name: 'Verify Deposit Payment',
          description: 'Confirm deposit payment received',
          taskType: 'CONTROL' as const,
          order: 3,
          mandatory: true,
          requiresApproval: false,
          roleCode: 'FINANCE_ADMIN'
        },
        {
          code: 'APPROVE_FINANCING',
          name: 'Approve Financing Application',
          description: 'Review and approve customer financing if applicable',
          taskType: 'APPROVAL' as const,
          order: 4,
          mandatory: false,
          requiresApproval: true,
          roleCode: 'FINANCE_MANAGER'
        }
      ]
    },
    {
      code: 'IN_PRODUCTION',
      name: 'Vehicle in Production',
      description: 'Vehicle preparation, accessory installation, and quality inspection',
      order: 4,
      isInitial: false,
      isFinal: false,
      slaDays: 14,
      tasks: [
        {
          code: 'PREPARE_VEHICLE',
          name: 'Prepare Vehicle for Delivery',
          description: 'Clean, service, and prepare vehicle for customer',
          taskType: 'ACTION' as const,
          order: 1,
          mandatory: true,
          requiresApproval: false,
          roleCode: 'WORKSHOP_MANAGER'
        },
        {
          code: 'INSTALL_ACCESSORIES',
          name: 'Install Accessories and Options',
          description: 'Install all ordered accessories and options',
          taskType: 'ACTION' as const,
          order: 2,
          mandatory: false,
          requiresApproval: false,
          roleCode: 'WORKSHOP_TECH',
          dependencies: ['PREPARE_VEHICLE']
        },
        {
          code: 'QUALITY_INSPECTION',
          name: 'Quality Control Inspection',
          description: 'Complete quality control checklist and approve vehicle',
          taskType: 'CONTROL' as const,
          order: 3,
          mandatory: true,
          requiresApproval: true,
          roleCode: 'QC_INSPECTOR',
          dependencies: ['PREPARE_VEHICLE']
        }
      ]
    },
    {
      code: 'READY_FOR_DELIVERY',
      name: 'Ready for Delivery',
      description: 'Delivery scheduling and final payment verification',
      order: 5,
      isInitial: false,
      isFinal: false,
      slaHours: 72,
      tasks: [
        {
          code: 'SCHEDULE_DELIVERY',
          name: 'Schedule Delivery Appointment',
          description: 'Coordinate delivery date and time with customer',
          taskType: 'ACTION' as const,
          order: 1,
          mandatory: true,
          requiresApproval: false,
          roleCode: 'DELIVERY_COORDINATOR'
        },
        {
          code: 'VERIFY_FINAL_PAYMENT',
          name: 'Verify Final Payment Complete',
          description: 'Confirm all payments have been received',
          taskType: 'CONTROL' as const,
          order: 2,
          mandatory: true,
          requiresApproval: false,
          roleCode: 'FINANCE_ADMIN'
        },
        {
          code: 'UPLOAD_REGISTRATION',
          name: 'Upload Vehicle Registration',
          description: 'Upload completed vehicle registration documents',
          taskType: 'DOCUMENT' as const,
          order: 3,
          mandatory: true,
          requiresApproval: false,
          roleCode: 'ADMIN_STAFF',
          requiredDocuments: ['VEHICLE_REGISTRATION']
        }
      ]
    },
    {
      code: 'DELIVERED',
      name: 'Vehicle Delivered',
      description: 'Vehicle handover to customer',
      order: 6,
      isInitial: false,
      isFinal: false,
      slaHours: 2,
      tasks: [
        {
          code: 'CONFIRM_DELIVERY',
          name: 'Confirm Vehicle Delivery',
          description: 'Handover vehicle keys and documents to customer',
          taskType: 'ACTION' as const,
          order: 1,
          mandatory: true,
          requiresApproval: false,
          roleCode: 'DELIVERY_AGENT'
        },
        {
          code: 'CAPTURE_SIGNATURE',
          name: 'Capture Customer Signature',
          description: 'Customer signs delivery acceptance form',
          taskType: 'DOCUMENT' as const,
          order: 2,
          mandatory: true,
          requiresApproval: false,
          roleCode: 'DELIVERY_AGENT',
          requiredDocuments: ['CUSTOMER_SIGNATURE']
        }
      ]
    },
    {
      code: 'INVOICED',
      name: 'Invoice Generated',
      description: 'Final invoice generation and order completion',
      order: 7,
      isInitial: false,
      isFinal: true,
      slaHours: 24,
      tasks: [
        {
          code: 'GENERATE_INVOICE',
          name: 'Generate Final Invoice',
          description: 'Generate and send final invoice to customer',
          taskType: 'ACTION' as const,
          order: 1,
          mandatory: true,
          requiresApproval: false,
          roleCode: 'FINANCE_ADMIN'
        }
      ]
    }
  ],

  transitions: [
    { name: 'Submit Order', fromStateCode: 'DRAFT', toStateCode: 'PENDING' },
    { name: 'Verification Complete', fromStateCode: 'PENDING', toStateCode: 'CONFIRMED' },
    { name: 'Reject - Invalid Info', fromStateCode: 'PENDING', toStateCode: 'DRAFT' },
    { name: 'Contract Signed', fromStateCode: 'CONFIRMED', toStateCode: 'IN_PRODUCTION' },
    { name: 'Contract Rejected', fromStateCode: 'CONFIRMED', toStateCode: 'PENDING' },
    { name: 'QC Approved', fromStateCode: 'IN_PRODUCTION', toStateCode: 'READY_FOR_DELIVERY' },
    { name: 'Delivery Scheduled', fromStateCode: 'READY_FOR_DELIVERY', toStateCode: 'DELIVERED' },
    { name: 'Delivery Confirmed', fromStateCode: 'DELIVERED', toStateCode: 'INVOICED' }
  ]
};

async function createCompleteVNWorkflow() {
  console.log('🚀 Starting VN Workflow Creation...\n');

  try {
    // Step 1: Create all states
    console.log('📝 Creating states...');
    for (const state of VN_WORKFLOW_SPECIFICATION.states) {
      console.log(`  Creating state: ${state.name} (${state.code})`);

      const statePayload = {
        stateCode: state.code,
        stateName: state.name,
        description: state.description,
        stateOrder: state.order,
        stateType: state.isInitial ? 'START' : (state.isFinal ? 'FINAL' : 'NORMAL'),
        isFinalState: state.isFinal,
        expectedDurationHours: state.slaHours || (state.slaDays ? state.slaDays * 24 : undefined),
      };

      const response = await fetch(`${WORKFLOW_SERVICE_URL}/workflow/processes/${WORKFLOW_CODE}/states`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(statePayload),
      });

      if (!response.ok) {
        const error = await response.text();
        console.error(`  ❌ Failed to create state ${state.code}:`, error);
        continue;
      }

      console.log(`  ✅ State ${state.code} created`);

      // Step 2: Create tasks for this state
      if (state.tasks && state.tasks.length > 0) {
        console.log(`  📋 Creating ${state.tasks.length} tasks for ${state.code}...`);

        for (const task of state.tasks) {
          const taskPayload = {
            taskCode: task.code,
            taskName: task.name,
            description: task.description,
            taskType: task.taskType,
            taskOrder: task.order,
            assignedToRole: task.roleCode,
            isMandatory: task.mandatory,
            requiresApproval: task.requiresApproval,
            slaHours: task.slaHours,
            requiredDocuments: task.requiredDocuments,
            configuration: task.dependencies ? { dependencies: task.dependencies } : undefined,
          };

          const taskResponse = await fetch(
            `${WORKFLOW_SERVICE_URL}/workflow/processes/${WORKFLOW_CODE}/states/${state.code}/tasks`,
            {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(taskPayload),
            }
          );

          if (!taskResponse.ok) {
            const error = await taskResponse.text();
            console.error(`    ❌ Failed to create task ${task.code}:`, error);
            continue;
          }

          console.log(`    ✅ Task ${task.code} created`);
        }
      }
    }

    // Step 3: Create all transitions
    console.log('\n🔗 Creating transitions...');
    for (const transition of VN_WORKFLOW_SPECIFICATION.transitions) {
      console.log(`  Creating transition: ${transition.fromStateCode} → ${transition.toStateCode}`);

      const transitionPayload = {
        transitionName: transition.name,
        fromStateCode: transition.fromStateCode,
        toStateCode: transition.toStateCode,
      };

      const response = await fetch(`${WORKFLOW_SERVICE_URL}/workflow/processes/${WORKFLOW_CODE}/transitions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(transitionPayload),
      });

      if (!response.ok) {
        const error = await response.text();
        console.error(`  ❌ Failed to create transition:`, error);
        continue;
      }

      console.log(`  ✅ Transition created`);
    }

    console.log('\n✅ VN Workflow created successfully!');
    console.log('\n📊 Summary:');
    console.log(`  - States: ${VN_WORKFLOW_SPECIFICATION.states.length}`);
    console.log(`  - Tasks: ${VN_WORKFLOW_SPECIFICATION.states.reduce((sum, s) => sum + (s.tasks?.length || 0), 0)}`);
    console.log(`  - Transitions: ${VN_WORKFLOW_SPECIFICATION.transitions.length}`);
    console.log(`\n🌐 View in UI: http://localhost:3001/dashboard/workflows/builder?code=${WORKFLOW_CODE}`);

  } catch (error) {
    console.error('\n❌ Error creating workflow:', error);
    process.exit(1);
  }
}

// Run the script
createCompleteVNWorkflow();
