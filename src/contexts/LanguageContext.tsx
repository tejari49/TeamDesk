import { createContext, useContext, useMemo, type ReactNode } from 'react';
import { useAuth } from './AuthContext';

const dictionary = {
  de: {
    today: 'Heute',
    groups: 'Gruppen',
    handovers: 'Handovers',
    team: 'Team',
    links: 'Links',
    chat: 'Chat',
    settings: 'Einstellungen',
    admin: 'Master Admin',
    releases: 'Release Verlauf',
    notAuthorized: 'Nicht berechtigt.'
  },
  en: {
    today: 'Today',
    groups: 'Groups',
    handovers: 'Handovers',
    team: 'Team',
    links: 'Links',
    chat: 'Chat',
    settings: 'Settings',
    admin: 'Master Admin',
    releases: 'Release History',
    notAuthorized: 'Not authorized.'
  }
} as const;

const LanguageContext = createContext<{ lang: 'de' | 'en'; t: (k: keyof (typeof dictionary)['de']) => string }>({
  lang: 'de',
  t: (k) => dictionary.de[k]
});

export const LanguageProvider = ({ children }: { children: ReactNode }) => {
  const { profile } = useAuth();
  const lang = (profile?.language ?? 'de') as 'de' | 'en';
  const value = useMemo(() => ({ lang, t: (k: keyof (typeof dictionary)['de']) => dictionary[lang][k] }), [lang]);
  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>;
};

export const useLanguage = () => useContext(LanguageContext);
