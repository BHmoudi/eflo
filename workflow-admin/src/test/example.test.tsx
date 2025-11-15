import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';

// Example component to test
function ExampleComponent({ message }: { message: string }) {
  return <div role="main">{message}</div>;
}

describe('Example Test Suite', () => {
  it('renders a message', () => {
    render(<ExampleComponent message="Hello, Eflo!" />);
    expect(screen.getByRole('main')).toHaveTextContent('Hello, Eflo!');
  });

  it('performs basic math', () => {
    expect(2 + 2).toBe(4);
  });
});
