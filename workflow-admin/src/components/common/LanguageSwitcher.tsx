'use client';

import { useState, useRef, useEffect } from 'react';
import { Globe, Check } from 'lucide-react';

// Define locales directly in component since we're using client-side only
const locales = ['en', 'fr', 'es', 'it', 'nl'] as const;
type Locale = (typeof locales)[number];

const localeNames: Record<Locale, string> = {
  en: 'English',
  fr: 'Français',
  es: 'Español',
  it: 'Italiano',
  nl: 'Dutch',
};

const localeFlags: Record<Locale, string> = {
  en: '🇬🇧',
  fr: '🇫🇷',
  es: '🇪🇸',
  it: '🇮🇹',
  nl: '🇳🇱',
};

export default function LanguageSwitcher() {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // Get current locale from localStorage
  const getCurrentLocale = (): Locale => {
    if (typeof window !== 'undefined') {
      const saved = localStorage.getItem('locale');
      if (saved && locales.includes(saved as Locale)) {
        return saved as Locale;
      }
    }
    return 'en';
  };

  const [currentLocale, setCurrentLocale] = useState<Locale>('en');

  // Load saved locale on mount
  useEffect(() => {
    setCurrentLocale(getCurrentLocale());
  }, []);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLocaleChange = (locale: Locale) => {
    console.log('Changing language to:', locale);

    // Save to localStorage
    localStorage.setItem('locale', locale);
    setCurrentLocale(locale);
    setIsOpen(false);

    // Dispatch custom event to notify other components
    window.dispatchEvent(new Event('localeChange'));

    // Reload page to apply translations
    setTimeout(() => {
      window.location.reload();
    }, 100);
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center space-x-2 px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
        aria-label="Change language"
      >
        <Globe className="w-5 h-5 text-gray-600 dark:text-gray-400" />
        <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
          {localeFlags[currentLocale]} {localeNames[currentLocale]}
        </span>
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-lg shadow-xl border border-gray-200 dark:border-gray-700 py-2 z-50 animate-scale-in">
          {locales.map((locale) => (
            <button
              key={locale}
              onClick={() => handleLocaleChange(locale)}
              className={`w-full flex items-center justify-between px-4 py-2.5 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors ${
                currentLocale === locale ? 'bg-blue-50 dark:bg-blue-900/20' : ''
              }`}
            >
              <div className="flex items-center space-x-3">
                <span className="text-xl">{localeFlags[locale]}</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white">
                  {localeNames[locale]}
                </span>
              </div>
              {currentLocale === locale && (
                <Check className="w-4 h-4 text-blue-600 dark:text-blue-400" />
              )}
            </button>
          ))}
        </div>
      )}

      <style jsx global>{`
        @keyframes scale-in {
          0% {
            opacity: 0;
            transform: scale(0.95);
          }
          100% {
            opacity: 1;
            transform: scale(1);
          }
        }

        .animate-scale-in {
          animation: scale-in 0.15s ease-out;
        }
      `}</style>
    </div>
  );
}
