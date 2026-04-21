export interface AddressItem {
  id: string;
  uid: string;
  name: string;
  phone: string;
  province?: string;
  city?: string;
  district?: string;
  address: string;
  isDefault?: boolean;
}

export interface AddressForm {
  id: string;
  uid: string;
  name: string;
  phone: string;
  province?: string;
  city?: string;
  district?: string;
  address: string;
  isDefault?: boolean;
}
  
export interface ApiResponse<T = any> {
    code: number
    message: string
    result: T
}