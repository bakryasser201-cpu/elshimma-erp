"use client";

import { useLanguage } from "@/contexts/language-context";
import { cn } from "@/lib/utils";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  DollarSign,
  ShoppingCart,
  Package,
  Users,
  TrendingUp,
  TrendingDown,
  ArrowUpRight,
  ArrowDownRight,
  Plus,
  FileText,
  UserPlus,
  Truck,
  Clock,
  CheckCircle2,
  AlertCircle,
  XCircle,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { RevenueChart } from "@/components/dashboard/charts/revenue-chart";
import { OrdersChart } from "@/components/dashboard/charts/orders-chart";

interface StatCardProps {
  title: string;
  value: string;
  change: number;
  changeLabel: string;
  icon: React.ElementType;
  trend: "up" | "down";
}

function StatCard({ title, value, change, changeLabel, icon: Icon, trend }: StatCardProps) {
  return (
    <Card className="relative overflow-hidden">
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">{title}</CardTitle>
        <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary/10">
          <Icon className="h-5 w-5 text-primary" />
        </div>
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        <div className="flex items-center gap-1 mt-1">
          {trend === "up" ? (
            <ArrowUpRight className="h-4 w-4 text-green-500" />
          ) : (
            <ArrowDownRight className="h-4 w-4 text-red-500" />
          )}
          <span className={cn("text-sm font-medium", trend === "up" ? "text-green-500" : "text-red-500")}>
            {change > 0 ? "+" : ""}{change}%
          </span>
          <span className="text-xs text-muted-foreground">{changeLabel}</span>
        </div>
      </CardContent>
    </Card>
  );
}

const recentOrders = [
  { id: "#ORD-001", customer: "Ahmed Hassan", product: "Laptop Pro X1", amount: "$1,234.00", status: "completed" },
  { id: "#ORD-002", customer: "Sara Mohamed", product: "Office Chair", amount: "$299.00", status: "processing" },
  { id: "#ORD-003", customer: "Mahmoud Ali", product: "Standing Desk", amount: "$599.00", status: "pending" },
  { id: "#ORD-004", customer: "Fatma Ibrahim", product: "Monitor 27\"", amount: "$449.00", status: "completed" },
  { id: "#ORD-005", customer: "Omar Youssef", product: "Keyboard RGB", amount: "$149.00", status: "cancelled" },
];

const inventoryItems = [
  { name: "Electronics", stock: 85, total: 100, status: "good" },
  { name: "Furniture", stock: 23, total: 50, status: "low" },
  { name: "Office Supplies", stock: 156, total: 200, status: "good" },
  { name: "Accessories", stock: 8, total: 100, status: "critical" },
];

const activities = [
  { action: "New order received", detail: "Order #ORD-006 from Nour Ahmed", time: "2 min ago", type: "order" },
  { action: "Payment confirmed", detail: "$1,234.00 for Order #ORD-001", time: "15 min ago", type: "payment" },
  { action: "Low stock alert", detail: "Furniture category below threshold", time: "1 hour ago", type: "alert" },
  { action: "Employee added", detail: "Mohamed Salah joined HR department", time: "3 hours ago", type: "employee" },
  { action: "Shipment delivered", detail: "Order #ORD-098 delivered successfully", time: "5 hours ago", type: "delivery" },
];

const statusStyles = {
  completed: { bg: "bg-green-500/10", text: "text-green-500", icon: CheckCircle2 },
  processing: { bg: "bg-blue-500/10", text: "text-blue-500", icon: Clock },
  pending: { bg: "bg-yellow-500/10", text: "text-yellow-500", icon: AlertCircle },
  cancelled: { bg: "bg-red-500/10", text: "text-red-500", icon: XCircle },
};

export default function DashboardPage() {
  const { t, direction } = useLanguage();

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">{t("dashboard")}</h1>
          <p className="text-muted-foreground">Welcome back! Here&apos;s your business overview.</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm">
            <FileText className="mr-2 h-4 w-4" />
            {t("export")}
          </Button>
          <Button size="sm">
            <Plus className="mr-2 h-4 w-4" />
            {t("addNew")}
          </Button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title={t("totalRevenue")}
          value="$124,592"
          change={12.5}
          changeLabel={t("thisMonth")}
          icon={DollarSign}
          trend="up"
        />
        <StatCard
          title={t("totalOrders")}
          value="1,284"
          change={8.2}
          changeLabel={t("thisMonth")}
          icon={ShoppingCart}
          trend="up"
        />
        <StatCard
          title={t("inventoryItems")}
          value="3,456"
          change={-2.4}
          changeLabel={t("thisMonth")}
          icon={Package}
          trend="down"
        />
        <StatCard
          title={t("totalEmployees")}
          value="48"
          change={4.1}
          changeLabel={t("thisMonth")}
          icon={Users}
          trend="up"
        />
      </div>

      {/* Charts Row */}
      <div className="grid gap-4 lg:grid-cols-7">
        <Card className="lg:col-span-4">
          <CardHeader>
            <CardTitle>{t("revenueOverview")}</CardTitle>
            <CardDescription>Monthly revenue for the current year</CardDescription>
          </CardHeader>
          <CardContent>
            <RevenueChart />
          </CardContent>
        </Card>
        <Card className="lg:col-span-3">
          <CardHeader>
            <CardTitle>{t("ordersAnalytics")}</CardTitle>
            <CardDescription>Order distribution by status</CardDescription>
          </CardHeader>
          <CardContent>
            <OrdersChart />
          </CardContent>
        </Card>
      </div>

      {/* Bottom Row */}
      <div className="grid gap-4 lg:grid-cols-3">
        {/* Recent Orders */}
        <Card className="lg:col-span-2">
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle>{t("recentOrders")}</CardTitle>
              <CardDescription>Latest orders from your store</CardDescription>
            </div>
            <Button variant="ghost" size="sm">
              {t("viewAll")}
            </Button>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {recentOrders.map((order) => {
                const statusStyle = statusStyles[order.status as keyof typeof statusStyles];
                const StatusIcon = statusStyle.icon;
                return (
                  <div
                    key={order.id}
                    className="flex items-center justify-between rounded-lg border border-border/50 p-3 transition-colors hover:bg-muted/50"
                  >
                    <div className="flex items-center gap-3">
                      <div className={cn("flex h-9 w-9 items-center justify-center rounded-full", statusStyle.bg)}>
                        <StatusIcon className={cn("h-4 w-4", statusStyle.text)} />
                      </div>
                      <div>
                        <p className="font-medium">{order.customer}</p>
                        <p className="text-sm text-muted-foreground">{order.product}</p>
                      </div>
                    </div>
                    <div className={cn("flex flex-col items-end gap-1", direction === "rtl" && "items-start")}>
                      <span className="font-semibold">{order.amount}</span>
                      <Badge variant="secondary" className={cn("text-xs capitalize", statusStyle.bg, statusStyle.text)}>
                        {order.status}
                      </Badge>
                    </div>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>

        {/* Inventory Status & Activity */}
        <div className="space-y-4">
          {/* Inventory Status */}
          <Card>
            <CardHeader>
              <CardTitle>{t("inventoryStatus")}</CardTitle>
              <CardDescription>Stock levels by category</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {inventoryItems.map((item) => (
                <div key={item.name} className="space-y-2">
                  <div className="flex items-center justify-between text-sm">
                    <span>{item.name}</span>
                    <span className="text-muted-foreground">
                      {item.stock}/{item.total}
                    </span>
                  </div>
                  <Progress
                    value={(item.stock / item.total) * 100}
                    className={cn(
                      "h-2",
                      item.status === "critical" && "[&>div]:bg-red-500",
                      item.status === "low" && "[&>div]:bg-yellow-500",
                      item.status === "good" && "[&>div]:bg-green-500"
                    )}
                  />
                </div>
              ))}
            </CardContent>
          </Card>

          {/* Quick Actions */}
          <Card>
            <CardHeader>
              <CardTitle>{t("quickActions")}</CardTitle>
            </CardHeader>
            <CardContent className="grid grid-cols-2 gap-2">
              <Button variant="outline" size="sm" className="h-auto flex-col gap-1 py-3">
                <Plus className="h-4 w-4" />
                <span className="text-xs">Add Order</span>
              </Button>
              <Button variant="outline" size="sm" className="h-auto flex-col gap-1 py-3">
                <Package className="h-4 w-4" />
                <span className="text-xs">Add Product</span>
              </Button>
              <Button variant="outline" size="sm" className="h-auto flex-col gap-1 py-3">
                <UserPlus className="h-4 w-4" />
                <span className="text-xs">Add Customer</span>
              </Button>
              <Button variant="outline" size="sm" className="h-auto flex-col gap-1 py-3">
                <Truck className="h-4 w-4" />
                <span className="text-xs">Add Supplier</span>
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Activity Timeline */}
      <Card>
        <CardHeader>
          <CardTitle>{t("activityTimeline")}</CardTitle>
          <CardDescription>Recent activities across your organization</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {activities.map((activity, index) => (
              <div key={index} className="flex items-start gap-4">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10">
                  {activity.type === "order" && <ShoppingCart className="h-4 w-4 text-primary" />}
                  {activity.type === "payment" && <DollarSign className="h-4 w-4 text-green-500" />}
                  {activity.type === "alert" && <AlertCircle className="h-4 w-4 text-yellow-500" />}
                  {activity.type === "employee" && <Users className="h-4 w-4 text-blue-500" />}
                  {activity.type === "delivery" && <Truck className="h-4 w-4 text-purple-500" />}
                </div>
                <div className="flex-1 space-y-1">
                  <p className="font-medium leading-none">{activity.action}</p>
                  <p className="text-sm text-muted-foreground">{activity.detail}</p>
                </div>
                <span className="text-xs text-muted-foreground">{activity.time}</span>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
