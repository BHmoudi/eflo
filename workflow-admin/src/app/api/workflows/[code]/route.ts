import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function GET(
  request: NextRequest,
  { params }: { params: { code: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { code } = params;

    // Fetch process
    const processResponse = await fetch(`${GATEWAY_URL}/workflow/processes/code/${code}`, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!processResponse.ok) {
      const errorText = await processResponse.text();
      return NextResponse.json({ error: errorText || 'Failed to fetch workflow' }, { status: processResponse.status });
    }

    const process = await processResponse.json();

    // Fetch states with tasks
    const statesResponse = await fetch(`${GATEWAY_URL}/workflow/processes/${code}/states`, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    let states = [];
    if (statesResponse.ok) {
      const rawStates = await statesResponse.json();
      // Transform backend field names to frontend field names
      states = rawStates.map((state: any) => ({
        ...state,
        code: state.stateCode,
        name: state.stateName,
        order: state.stateOrder,
        isFinal: state.isFinalState,
        isInitial: state.stateType === 'START',
        tasks: (state.tasks || []).map((task: any) => ({
          ...task,
          code: task.taskCode,
          name: task.taskName,
          order: task.taskOrder,
          mandatory: task.isMandatory,
        })),
      }));
    }

    // Fetch transitions
    const transitionsResponse = await fetch(`${GATEWAY_URL}/workflow/processes/${code}/transitions`, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    let transitions = [];
    if (transitionsResponse.ok) {
      const rawTransitions = await transitionsResponse.json();
      // Transform backend field names to frontend field names
      transitions = rawTransitions.map((transition: any) => ({
        ...transition,
        name: transition.transitionName,
        fromStateCode: transition.fromStateCode,
        toStateCode: transition.toStateCode,
      }));
    }

    // Combine all data
    const completeWorkflow = {
      ...process,
      code: process.processCode,
      name: process.processName,
      states: states,
      transitions: transitions,
    };

    return NextResponse.json(completeWorkflow);
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to fetch workflow' }, { status: 500 });
  }
}

export async function PUT(
  request: NextRequest,
  { params }: { params: { code: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { code } = params;
    const body = await request.json();

    const response = await fetch(`${GATEWAY_URL}/workflow/processes/${code}`, {
      method: 'PUT',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to update workflow' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to update workflow' }, { status: 500 });
  }
}

export async function PATCH(
  request: NextRequest,
  { params }: { params: { code: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { code } = params;
    const body = await request.json();

    const response = await fetch(`${GATEWAY_URL}/workflow/processes/${code}`, {
      method: 'PATCH',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to update workflow' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to update workflow' }, { status: 500 });
  }
}

export async function DELETE(
  request: NextRequest,
  { params }: { params: { code: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { code } = params;

    const response = await fetch(`${GATEWAY_URL}/workflow/processes/code/${code}`, {
      method: 'DELETE',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to delete workflow' }, { status: response.status });
    }

    return NextResponse.json({ success: true });
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to delete workflow' }, { status: 500 });
  }
}
