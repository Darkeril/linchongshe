// src/type/map.ts
export interface LocationData {
  name: string;
  address: string;
  province: string;
  city: string;
  district?: string;
  location: {
    lng: number;
    lat: number;
  };
}