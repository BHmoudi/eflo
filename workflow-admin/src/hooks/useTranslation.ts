import { useState, useEffect } from 'react';

// Import all translations statically
import enMessages from '@/i18n/locales/en.json';
import frMessages from '@/i18n/locales/fr.json';
import esMessages from '@/i18n/locales/es.json';
import itMessages from '@/i18n/locales/it.json';
import nlMessages from '@/i18n/locales/nl.json';

type Locale = 'en' | 'fr' | 'es' | 'it' | 'nl';
type Messages = Record<string, any>;

// All messages preloaded
const allMessages: Record<Locale, Messages> = {
  en: enMessages,
  fr: frMessages,
  es: esMessages,
  it: itMessages,
  nl: nlMessages,
};

export function useTranslation() {
  const [locale, setLocale] = useState<Locale>('en');

  useEffect(() => {
    // Load locale from localStorage on mount
    const savedLocale = (localStorage.getItem('locale') as Locale) || 'en';
    setLocale(savedLocale);
  }, []);

  // Translation function
  const t = (key: string, fallback?: string): string => {
    const messages = allMessages[locale] || allMessages['en'];
    const keys = key.split('.');
    let value: any = messages;

    for (const k of keys) {
      if (value && typeof value === 'object' && k in value) {
        value = value[k];
      } else {
        return fallback || key;
      }
    }

    return typeof value === 'string' ? value : fallback || key;
  };

  // Change locale function
  const changeLocale = (newLocale: Locale) => {
    localStorage.setItem('locale', newLocale);
    setLocale(newLocale);
    window.location.reload();
  };

  return {
    locale,
    t,
    changeLocale,
  };
}
