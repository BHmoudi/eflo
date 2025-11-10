import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function PUT(
  request: NextRequest,
  { params }: { params: { code: string; stateCode: string; taskCode: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { code, stateCode, taskCode } = params;
    const body = await request.json();

    // Transform frontend field names to backend field names
    const transformedBody = {
      taskCode: body.code || taskCode,
      taskName: body.name,
      description: body.description,
      taskType: body.taskType,
      taskOrder: body.order,
      assignedToRole: body.roleCode,
      isMandatory: body.mandatory !== undefined ? body.mandatory : true,
      requiresApproval: body.requiresApproval || false,
      expectedDurationHours: body.slaHours,
      slaHours: body.slaHours,
      autoAssign: body.autoAssign !== undefined ? body.autoAssign : true,
      requiredDocuments: body.requiredDocuments,
      inputFields: body.inputFields,
      formDefinition: body.formDefinition,
      validationRules: body.validationRules,
      configuration: body.configuration,
      approvalChainId: body.approvalChainId,
      dependencies: body.dependencies,
      conditions: body.conditions,
    };

    const response = await fetch(
      `${GATEWAY_URL}/workflow/processes/${code}/states/${stateCode}/tasks/${taskCode}`,
      {
        method: 'PUT',
        headers: {
          'Authorization': token || '',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(transformedBody),
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to update task' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to update task' }, { status: 500 });
  }
}

export async function DELETE(
  request: NextRequest,
  { params }: { params: { code: string; stateCode: string; taskCode: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { code, stateCode, taskCode } = params;

    const response = await fetch(
      `${GATEWAY_URL}/workflow/processes/${code}/states/${stateCode}/tasks/${taskCode}`,
      {
        method: 'DELETE',
        headers: {
          'Authorization': token || '',
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to delete task' }, { status: response.status });
    }

    return NextResponse.json({ success: true });
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to delete task' }, { status: 500 });
  }
}
