import AMapLoader from "@amap/amap-jsapi-loader";

// 类型定义
interface LocationOptions {
  enableHighAccuracy: boolean;
  timeout: number;
  offset: [number, number];
  zoomToAccuracy: boolean;
  position: 'RB' | 'RT' | 'LB' | 'LT';
}

interface CityResult {
  city: string;
  citycode: string;
  province: string;
  [key: string]: any;
}

interface LocationResult {
  position: [number, number];
  city: string;
  [key: string]: any;
}

interface SearchResult {
  poiList: {
    pois: Array<{
      id: string;
      name: string;
      address: string;
      location: {
        lng: number;
        lat: number;
      };
    }>;
  };
}

interface PlaceResult {
  poiList: {
    pois: Array<any>;
    [key: string]: any;
  };
  [key: string]: any;
}

interface GeocoderResult {
  regeocode: {
    formattedAddress: string;
    addressComponent: any;
    [key: string]: any;
  };
  [key: string]: any;
}

// 常量定义
const MAP_KEY = import.meta.env.VITE_AMAP_KEY;
const DEFAULT_VERSION = "2.0";

// 通用配置
const DEFAULT_LOCATION_OPTIONS: LocationOptions = {
  enableHighAccuracy: true,
  timeout: 10000,
  offset: [10, 20],
  zoomToAccuracy: true,
  position: 'RB'
};

const gdMapUtil = {
  map: null as any,

  // 初始化地图
  async initAMap(): Promise<any> {
    try {
      return await AMapLoader.load({
        key: MAP_KEY,
        version: DEFAULT_VERSION,
        plugins: [],
      });
    } catch (error: any) {
      throw new Error(`初始化地图失败: ${error.message}`);
    }
  },

  // 通用插件加载方法
  async loadPlugin<T>(pluginName: string | string[], callback: (AMap: any, resolve: (value: T) => void, reject: (reason: Error) => void) => void): Promise<T> {
    try {
      const AMap = await this.initAMap();
      return new Promise<T>((resolve, reject) => {
        AMap.plugin(Array.isArray(pluginName) ? pluginName : [pluginName], () => {
          callback(AMap, resolve, reject);
        });
      });
    } catch (error: any) {
      throw new Error(`加载插件 ${pluginName} 失败: ${error.message}`);
    }
  },

  // 获取城市信息
  async getCity(): Promise<CityResult> {
    return this.loadPlugin('AMap.CitySearch', (AMap, resolve, reject) => {
      const citySearch = new AMap.CitySearch(DEFAULT_LOCATION_OPTIONS);
      citySearch.getLocalCity((status: string, result: CityResult) => {
        status === 'complete' ? resolve(result) : reject(new Error('获取城市信息失败'));
      });
    });
  },

  // 获取定位城市
  async getLocationCity(): Promise<LocationResult> {
    return this.loadPlugin('AMap.Geolocation', (AMap, resolve, reject) => {
      const geolocation = new AMap.Geolocation(DEFAULT_LOCATION_OPTIONS);
      geolocation.getCityInfo((status: string, result: LocationResult) => {
        status === 'complete' ? resolve(result) : reject(new Error('获取定位城市失败'));
      });
    });
  },

  // 获取搜索建议
  async getSearchOptions(query: string): Promise<SearchResult> {
    return this.loadPlugin('AMap.AutoComplete', (AMap, resolve, reject) => {
      const autocomplete = new AMap.AutoComplete();
      autocomplete.search(query, (status: string, result: SearchResult) => {
        status === 'complete' ? resolve(result) : reject(new Error('获取搜索建议失败'));
      });
    });
  },

  // 设置标记点
  async setMarker(lng: number, lat: number): Promise<any> {
    return this.loadPlugin('AMap.Marker', (AMap, resolve) => {
      const marker = new AMap.Marker({
        position: new AMap.LngLat(lng, lat),
      });
      resolve(marker);
    });
  },

  // 地点搜索
  async getPlaceSearch(keyword: string): Promise<PlaceResult> {
    return this.loadPlugin('AMap.PlaceSearch', (AMap, resolve, reject) => {
      const placeSearch = new AMap.PlaceSearch();
      placeSearch.search(keyword, (status: string, result: PlaceResult) => {
        status === 'complete' ? resolve(result) : reject(new Error('地点搜索失败'));
      });
    });
  },

  // 附近搜索（根据经纬度搜索附近的POI）
  async searchNearby(lng: number, lat: number, radius: number = 300): Promise<PlaceResult> {
    return this.loadPlugin('AMap.PlaceSearch', (AMap, resolve, reject) => {
      const placeSearch = new AMap.PlaceSearch({
        city: '',
        type: '', // 搜索所有类型
        pageSize: 10, // 最多返回10个结果
        pageIndex: 1,
        citylimit: false,
        extensions: 'base',
      });
      // 使用附近搜索API
      placeSearch.searchNearBy('', [lng, lat], radius, (status: string, result: PlaceResult) => {
        status === 'complete' ? resolve(result) : reject(new Error('附近搜索失败'));
      });
    });
  },

  // 获取地点详情
  async getPlaceSearchDetail(poiid: string): Promise<PlaceResult> {
    return this.loadPlugin('AMap.PlaceSearch', (AMap, resolve, reject) => {
      const placeSearch = new AMap.PlaceSearch();
      placeSearch.getDetails(poiid, (status: string, result: PlaceResult) => {
        status === 'complete' ? resolve(result) : reject(new Error('获取地点详情失败'));
      });
    });
  },

  // 地理编码获取地址
  async GeocoderGetAddress(lng: number, lat: number): Promise<GeocoderResult> {
    return this.loadPlugin('AMap.Geocoder', (AMap, resolve, reject) => {
      const geocoder = new AMap.Geocoder({});
      geocoder.getAddress([lng, lat], (status: string, result: GeocoderResult) => {
        status === 'complete' && result.info === 'OK'
          ? resolve(result)
          : reject(new Error('地理编码获取地址失败'));
      });
    });
  },

  async getCurrentPosition(): Promise<{ position: { lng: number; lat: number } }> {
    return this.loadPlugin('AMap.Geolocation', (AMap, resolve, reject) => {
      const geolocation = new AMap.Geolocation(DEFAULT_LOCATION_OPTIONS);
      geolocation.getCurrentPosition((status: string, result: any) => {
        if (status === 'complete' && result.position) {
          resolve({ position: result.position });
        } else {
          reject(result);
        }
      });
    });
  },

  // 销毁地图实例
  destroyMap(): void {
    if (this.map) {
      this.map.destroy();
      this.map = null;
    }
  },
};

export default gdMapUtil;