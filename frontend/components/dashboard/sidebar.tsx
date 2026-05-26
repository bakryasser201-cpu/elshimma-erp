"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  Package,
  ShoppingCart,
  Warehouse,
  DollarSign,
  Users,
  UserCircle,
  Truck,
  BarChart3,
  Settings,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { useLanguage } from "@/contexts/language-context";
import { Button } from "@/components/ui/button";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import { ScrollArea } from "@/components/ui/scroll-area";

interface NavItem {
  key: string;
  icon: React.ElementType;
  href: string;
}

const navItems: NavItem[] = [
  { key: "dashboard", icon: LayoutDashboard, href: "/dashboard" },
  { key: "inventory", icon: Package, href: "/dashboard/inventory" },
  { key: "orders", icon: ShoppingCart, href: "/dashboard/orders" },
  { key: "warehouse", icon: Warehouse, href: "/dashboard/warehouse" },
  { key: "finance", icon: DollarSign, href: "/dashboard/finance" },
  { key: "hr", icon: Users, href: "/dashboard/hr" },
  { key: "customers", icon: UserCircle, href: "/dashboard/customers" },
  { key: "suppliers", icon: Truck, href: "/dashboard/suppliers" },
  { key: "analytics", icon: BarChart3, href: "/dashboard/analytics" },
  { key: "settings", icon: Settings, href: "/dashboard/settings" },
];

interface SidebarProps {
  collapsed: boolean;
  onToggle: () => void;
}

export function Sidebar({ collapsed, onToggle }: SidebarProps) {
  const pathname = usePathname();
  const { t, direction } = useLanguage();

  return (
    <TooltipProvider delayDuration={0}>
      <aside
        className={cn(
          "fixed top-0 h-screen border-r border-sidebar-border bg-sidebar transition-all duration-300 z-40",
          collapsed ? "w-16" : "w-64",
          direction === "rtl" ? "right-0 border-l border-r-0" : "left-0"
        )}
      >
        <div className="flex h-16 items-center justify-between border-b border-sidebar-border px-4">
          {!collapsed && (
            <Link href="/dashboard" className="flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground font-bold text-sm">
                ES
              </div>
              <span className="font-semibold text-sidebar-foreground">ElShimma ERP</span>
            </Link>
          )}
          {collapsed && (
            <Link href="/dashboard" className="mx-auto flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground font-bold text-sm">
              ES
            </Link>
          )}
        </div>

        <ScrollArea className="h-[calc(100vh-8rem)]">
          <nav className="flex flex-col gap-1 p-3">
            {navItems.map((item) => {
              const isActive = pathname === item.href || (item.href !== "/dashboard" && pathname.startsWith(item.href));
              const Icon = item.icon;

              const linkContent = (
                <Link
                  href={item.href}
                  className={cn(
                    "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200",
                    isActive
                      ? "bg-sidebar-accent text-sidebar-primary"
                      : "text-sidebar-foreground/70 hover:bg-sidebar-accent/50 hover:text-sidebar-foreground",
                    collapsed && "justify-center px-2"
                  )}
                >
                  <Icon className={cn("h-5 w-5 flex-shrink-0", isActive && "text-sidebar-primary")} />
                  {!collapsed && <span>{t(item.key)}</span>}
                </Link>
              );

              if (collapsed) {
                return (
                  <Tooltip key={item.key}>
                    <TooltipTrigger asChild>{linkContent}</TooltipTrigger>
                    <TooltipContent side={direction === "rtl" ? "left" : "right"} className="font-medium">
                      {t(item.key)}
                    </TooltipContent>
                  </Tooltip>
                );
              }

              return <div key={item.key}>{linkContent}</div>;
            })}
          </nav>
        </ScrollArea>

        <div className="absolute bottom-0 left-0 right-0 border-t border-sidebar-border p-3">
          <Button
            variant="ghost"
            size="sm"
            className={cn(
              "w-full justify-center text-sidebar-foreground/70 hover:text-sidebar-foreground hover:bg-sidebar-accent/50",
              collapsed && "px-2"
            )}
            onClick={onToggle}
          >
            {collapsed ? (
              direction === "rtl" ? <ChevronLeft className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />
            ) : (
              <>
                {direction === "rtl" ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
                {!collapsed && <span className="ml-2">{direction === "rtl" ? "طي القائمة" : "Collapse"}</span>}
              </>
            )}
          </Button>
        </div>
      </aside>
    </TooltipProvider>
  );
}
