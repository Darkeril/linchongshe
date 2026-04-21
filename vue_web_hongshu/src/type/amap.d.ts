// src/type/amap.d.ts
declare namespace AMap {
  class Geolocation {
    constructor(opts?: any);
    getCurrentPosition(callback: (status: string, result: any) => void): void;
    getCityInfo(callback: (status: string, result: any) => void): void;
  }
  export class Map {
    constructor(container: string | HTMLDivElement, opts?: any);
    on(event: string, handler: (...args: any[]) => void): void;
    off(event: string, handler: (...args: any[]) => void): void;
    setCenter(center: [number, number]): void;
    setZoom(zoom: number): void;
    add(overlay: any): void;
    remove(overlay: any): void;
    setFitView(): void;
  }
  export class Marker {
    constructor(opts?: any);
    setPosition(lnglat: [number, number]): void;
  }
  export interface Poi {
    id: string;
    name: string;
    address: string;
    location: { lng: number; lat: number } | [number, number];
  }
  export interface MapEvent {
    lnglat: {
      getLng(): number;
      getLat(): number;
    };
  }
  export interface ReGeocodeResult {
    regeocode: {
      formattedAddress: string;
      addressComponent: {
        province: string;
        city: string;
        district: string;
        street: string;
        streetNumber: string;
        neighborhood: string;
        building: string;
      };
    };
  }
}