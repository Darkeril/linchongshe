export interface UserLogin {
  phone: string;
  email: string;
  code: string;
  username: string;
  password: string;
  captchaVerification: string;
}

export interface User {
  phone: string;
  id: string;
  hsId: string;
  username: string;
  avatar: string;
  gender: number;
  description: string;
  birthday: string;
  province: string;
  city: string;
  district: string;
  address: string;
  userCover: string;
  noteCount: number;
  followerCount: number;
  fanCount: number;
}
