export interface ProductOrder {
  dealStatus: string
  orderStatus: string        
  postType: string          
  productPostStatus: number
  shipCompany?: string
  shipNum?: string
  postSelfCode?: string
  postUsername?: string
  postPhone?: string
  postAddress?: string
  shipUsername?: string
  shipPhone?: string
  shipAddress?: string
  orderNumber: string
  answerBuyMoney?: number
  evaScore: number
  evaContent: string
}

export interface PaymentOrder {
  payPrice: number
  payTypeName: string
  payType?: string | number  // 支付类型：'1'=微信支付，'2'=支付宝
}

export interface PaymentPay {
  finishTime: string | number
}