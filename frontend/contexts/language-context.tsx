"use client";

import { createContext, useContext, useState, useEffect, type ReactNode } from "react";

type Language = "en" | "ar";
type Direction = "ltr" | "rtl";

interface Translations {
  [key: string]: {
    en: string;
    ar: string;
  };
}

const translations: Translations = {
  // Navigation
  dashboard: { en: "Dashboard", ar: "لوحة التحكم" },
  inventory: { en: "Inventory", ar: "المخزون" },
  orders: { en: "Orders", ar: "الطلبات" },
  warehouse: { en: "Warehouse", ar: "المستودع" },
  finance: { en: "Finance", ar: "المالية" },
  hr: { en: "HR", ar: "الموارد البشرية" },
  customers: { en: "Customers", ar: "العملاء" },
  suppliers: { en: "Suppliers", ar: "الموردين" },
  analytics: { en: "Analytics", ar: "التحليلات" },
  settings: { en: "Settings", ar: "الإعدادات" },
  
  // Dashboard
  totalRevenue: { en: "Total Revenue", ar: "إجمالي الإيرادات" },
  totalOrders: { en: "Total Orders", ar: "إجمالي الطلبات" },
  inventoryItems: { en: "Inventory Items", ar: "عناصر المخزون" },
  totalEmployees: { en: "Total Employees", ar: "إجمالي الموظفين" },
  recentOrders: { en: "Recent Orders", ar: "الطلبات الأخيرة" },
  inventoryStatus: { en: "Inventory Status", ar: "حالة المخزون" },
  activityTimeline: { en: "Activity Timeline", ar: "الجدول الزمني للنشاط" },
  quickActions: { en: "Quick Actions", ar: "إجراءات سريعة" },
  revenueOverview: { en: "Revenue Overview", ar: "نظرة عامة على الإيرادات" },
  ordersAnalytics: { en: "Orders Analytics", ar: "تحليلات الطلبات" },
  
  // Common
  search: { en: "Search...", ar: "بحث..." },
  notifications: { en: "Notifications", ar: "الإشعارات" },
  profile: { en: "Profile", ar: "الملف الشخصي" },
  logout: { en: "Logout", ar: "تسجيل الخروج" },
  viewAll: { en: "View All", ar: "عرض الكل" },
  today: { en: "Today", ar: "اليوم" },
  thisWeek: { en: "This Week", ar: "هذا الأسبوع" },
  thisMonth: { en: "This Month", ar: "هذا الشهر" },
  
  // Status
  pending: { en: "Pending", ar: "قيد الانتظار" },
  processing: { en: "Processing", ar: "قيد المعالجة" },
  completed: { en: "Completed", ar: "مكتمل" },
  cancelled: { en: "Cancelled", ar: "ملغي" },
  inStock: { en: "In Stock", ar: "متوفر" },
  lowStock: { en: "Low Stock", ar: "مخزون منخفض" },
  outOfStock: { en: "Out of Stock", ar: "غير متوفر" },
  
  // Actions
  addNew: { en: "Add New", ar: "إضافة جديد" },
  edit: { en: "Edit", ar: "تعديل" },
  delete: { en: "Delete", ar: "حذف" },
  export: { en: "Export", ar: "تصدير" },
  import: { en: "Import", ar: "استيراد" },
  filter: { en: "Filter", ar: "تصفية" },
  
  // Auth
  login: { en: "Login", ar: "تسجيل الدخول" },
  register: { en: "Register", ar: "إنشاء حساب" },
  email: { en: "Email", ar: "البريد الإلكتروني" },
  password: { en: "Password", ar: "كلمة المرور" },
  confirmPassword: { en: "Confirm Password", ar: "تأكيد كلمة المرور" },
  forgotPassword: { en: "Forgot Password?", ar: "نسيت كلمة المرور؟" },
  rememberMe: { en: "Remember me", ar: "تذكرني" },
  welcomeBack: { en: "Welcome back", ar: "مرحباً بعودتك" },
  signInToContinue: { en: "Sign in to continue to ElShimma ERP", ar: "سجل الدخول للمتابعة إلى ElShimma ERP" },
  createAccount: { en: "Create an account", ar: "إنشاء حساب جديد" },
  getStarted: { en: "Get started with ElShimma ERP", ar: "ابدأ مع ElShimma ERP" },
  firstName: { en: "First Name", ar: "الاسم الأول" },
  lastName: { en: "Last Name", ar: "الاسم الأخير" },
  companyName: { en: "Company Name", ar: "اسم الشركة" },
  alreadyHaveAccount: { en: "Already have an account?", ar: "لديك حساب بالفعل؟" },
  dontHaveAccount: { en: "Don't have an account?", ar: "ليس لديك حساب؟" },
};

interface LanguageContextType {
  language: Language;
  direction: Direction;
  setLanguage: (lang: Language) => void;
  t: (key: string) => string;
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export function LanguageProvider({ children }: { children: ReactNode }) {
  const [language, setLanguageState] = useState<Language>("en");
  const direction: Direction = language === "ar" ? "rtl" : "ltr";

  useEffect(() => {
    const saved = localStorage.getItem("language") as Language;
    if (saved && (saved === "en" || saved === "ar")) {
      setLanguageState(saved);
    }
  }, []);

  useEffect(() => {
    document.documentElement.lang = language;
    document.documentElement.dir = direction;
    localStorage.setItem("language", language);
  }, [language, direction]);

  const setLanguage = (lang: Language) => {
    setLanguageState(lang);
  };

  const t = (key: string): string => {
    const translation = translations[key];
    if (!translation) return key;
    return translation[language] || key;
  };

  return (
    <LanguageContext.Provider value={{ language, direction, setLanguage, t }}>
      {children}
    </LanguageContext.Provider>
  );
}

export function useLanguage() {
  const context = useContext(LanguageContext);
  if (context === undefined) {
    throw new Error("useLanguage must be used within a LanguageProvider");
  }
  return context;
}
