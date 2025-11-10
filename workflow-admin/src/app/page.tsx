import { redirect } from 'next/navigation';

export default function HomePage() {
  // Simple server-side redirect to login
  // NO client-side checks, NO localStorage access
  // This only runs when accessing the root "/" path
  redirect('/login');
}
