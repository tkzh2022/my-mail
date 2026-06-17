export type UserRole = 'user' | 'merchant' | 'admin'

export interface User {
  id: number
  username: string
  email?: string
  phone?: string
  nickname?: string
  avatar?: string
  role: UserRole
  status?: number
  createdAt?: string
}

export interface Merchant {
  id: number
  userId: number
  shopName: string
  businessLicense: string
  contactName: string
  contactPhone: string
  description?: string
  logoUrl?: string
  status: number
  rating?: number
  rejectReason?: string
  approvedAt?: string
  createdAt?: string
}

export interface Category {
  id: number
  name: string
  parentId?: number
  level: number
  sortOrder?: number
  iconUrl?: string
  status: number
  children?: Category[]
}

export interface Product {
  id: number
  merchantId: number
  categoryId: number
  name: string
  subtitle?: string
  description?: string
  price: number
  originalPrice?: number
  stock: number
  salesCount?: number
  images: string[]
  status: number
  rejectReason?: string
  createdAt?: string
  updatedAt?: string
}

export interface CartItem {
  id: number
  productId: number
  productName: string
  productImage: string
  price: number
  quantity: number
  selected: boolean
  available: boolean
}

export interface OrderItem {
  id: number
  orderId: number
  productId: number
  productName: string
  productImage: string
  price: number
  quantity: number
  merchantId: number
}

export interface Order {
  id: number
  orderNo: string
  userId: number
  totalAmount: number
  payAmount: number
  freightAmount?: number
  status: number
  paymentMethod?: string
  paymentTime?: string
  shippingTime?: string
  deliveryTime?: string
  completeTime?: string
  trackingCompany?: string
  trackingNumber?: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark?: string
  items?: OrderItem[]
  createdAt?: string
  updatedAt?: string
}

export interface Payment {
  id: number
  orderId: number
  paymentNo: string
  tradeNo?: string
  paymentMethod: string
  amount: number
  status: number
  paidAt?: string
  createdAt?: string
}

export interface Refund {
  id: number
  refundNo: string
  orderId: number
  userId: number
  merchantId: number
  amount: number
  reason: string
  evidenceImages?: string[]
  status: number
  merchantReply?: string
  adminDecision?: string
  adminId?: number
  completedAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface Notification {
  id: number
  userId: number
  type: string
  channel: string
  title: string
  content: string
  isRead: boolean
  sentAt: string
  createdAt?: string
}

export interface Recommendation {
  id: number
  userId: number
  productId: number
  score: number
  algorithmVersion: string
  reason?: string
  product?: Product
  createdAt?: string
  expiresAt?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface ApiResponse<T> {
  code: number
  data: T
  message?: string
}
