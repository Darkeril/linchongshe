<template>
  <div id="guide-map" style="width: 100%; height: 100%">
    <div style="display: flex; justify-content: center">
      <el-select
        v-model="targetAddress"
        :placeholder="$t('note.searchAddress')"
        style="width: 60%; z-index: 1; margin-top: 50px"
        filterable
        remote
        value-key="id"
        :remote-method="getSearch"
        :loading="loading"
        @change="currentSelect"
      >
        <el-option v-for="item in addressOptions" :key="item.id" :label="item.name" :value="item">
          <span style="float: left">{{ item.name }}</span>
          <span style="float: right; color: #8492a6; font-size: 13px; margin-left: 10px">{{ item.address }}</span>
        </el-option>
      </el-select>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";

const { t } = useI18n();
import type { LocationData } from "@/type/map";
import gdMapUtil from "@/utils/gdMapUtil";
import { debounce } from "lodash-es";

// 响应式数据
const loading = ref(false);
const mapCenter = ref<[number, number]>([116.397428, 39.90923]);
const map = ref<AMap.Map | null>(null);
const marker = ref<AMap.Marker | null>(null);
const targetAddress = ref<LocationData | null>(null);
const addressOptions = ref<AMap.Poi[]>([]);

// 统一地址解析方法（异步，支持搜索附近POI）
const parseAddress = async (result: AMap.ReGeocodeResult, lng: number, lat: number, customName?: string): Promise<LocationData> => {
  const address = result.regeocode;
  const addressComponent = address.addressComponent;

  // 优先使用传入的地点名称（POI名称）
  let detailedName = customName;
  
  // 如果没有传入名称，尝试搜索附近POI获取地点名称
  if (!detailedName) {
    try {
      // 搜索附近300米内的POI
      const nearbyResult = await gdMapUtil.searchNearby(lng, lat, 300);
      if (nearbyResult?.poiList?.pois && nearbyResult.poiList.pois.length > 0) {
        // 找到距离最近的POI作为地点名称
        const nearestPoi = nearbyResult.poiList.pois[0];
        detailedName = nearestPoi.name;
        console.log('📍 找到附近POI:', nearestPoi.name);
      }
    } catch (error) {
      console.warn('附近POI搜索失败，使用备用方案:', error);
    }
  }
  
  // 如果还是没有，则使用小区、建筑物等信息
  if (!detailedName) {
    // 优先使用小区、建筑物等有意义的名称
    detailedName = addressComponent.neighborhood || addressComponent.building || "";
    
    // 如果连这些都没有，才使用详细地址组件（但不包括省市区）
    if (!detailedName) {
      const components = [
        addressComponent.street,
        addressComponent.streetNumber,
      ].filter(Boolean);
      
      detailedName = components.length > 0 ? components.join("") : "";
    }
  }

  // 提取详细地址（去掉省市区信息）
  const extractDetailedAddress = (): string => {
    // 优先使用街道、门牌号、小区、建筑物等信息
    const detailParts = [
      addressComponent.street,
      addressComponent.streetNumber,
      addressComponent.neighborhood,
      addressComponent.building,
    ].filter(Boolean);
    
    if (detailParts.length > 0) {
      return detailParts.join("");
    }
    
    // 如果没有详细地址信息，从完整地址中去掉省市区
    let detailedAddr = address.formattedAddress || "";
    const province = addressComponent.province || "";
    const city = addressComponent.city || addressComponent.province || "";
    const district = addressComponent.district || "";
    
    // 依次去掉省、市、区
    if (province && detailedAddr.startsWith(province)) {
      detailedAddr = detailedAddr.substring(province.length);
    }
    if (city && detailedAddr.startsWith(city)) {
      detailedAddr = detailedAddr.substring(city.length);
    }
    if (district && detailedAddr.startsWith(district)) {
      detailedAddr = detailedAddr.substring(district.length);
    }
    
    return detailedAddr.trim();
  };

  return {
    name: detailedName,
    address: extractDetailedAddress(),
    province: addressComponent.province,
    city: addressComponent.city || addressComponent.province,
    district: addressComponent.district,
    location: { lng, lat },
  };
};

// 点击地图获取位置
const handleMapClick = async (e: AMap.MapEvent) => {
  try {
    const lnglat = e.lnglat;
    const result = await gdMapUtil.GeocoderGetAddress(lnglat.getLng(), lnglat.getLat());
    if (result.regeocode) {
      const locationData = await parseAddress(result, lnglat.getLng(), lnglat.getLat());
      targetAddress.value = locationData;
      setMark(lnglat.getLng(), lnglat.getLat());
      emit("select-location", locationData);
    }
  } catch (error) {
    ElMessage.error(t("address.getLocationFailed"));
  }
};

// 初始化地图
const getMap = async () => {
  try {
    const AMap = await gdMapUtil.initAMap();
    map.value = new AMap.Map("guide-map", {
      viewMode: "2D",
      zoom: 12,
      center: mapCenter.value,
    });
    map.value?.on("click", handleMapClick);
  } catch (error) {
    ElMessage.error(t("address.mapInitFailed"));
    throw error;
  }
};

// 搜索地点
const getSearch = debounce(async (query: string) => {
  if (!query.trim()) {
    addressOptions.value = [];
    return;
  }

  loading.value = true;
  try {
    const res = await gdMapUtil.getPlaceSearch(query);
    addressOptions.value = res.poiList?.pois || [];
  } catch (error) {
    ElMessage.warning(t("address.searchFailed"));
    addressOptions.value = [];
  } finally {
    loading.value = false;
  }
}, 300);

// 设置标记点
const setMark = async (lng: number, lat: number) => {
  if (!map.value) return;

  if (marker.value) {
    map.value.remove(marker.value);
  }

  const newMarker = await gdMapUtil.setMarker(lng, lat);
  marker.value = newMarker;
  map.value.add(newMarker);
};

// 选择地点
const currentSelect = async (val: AMap.Poi) => {
  if (!val?.location) return;

  try {
    let lng: number, lat: number;
    if (Array.isArray(val.location)) {
      [lng, lat] = val.location;
    } else {
      lng = val.location.lng;
      lat = val.location.lat;
    }

    setMark(lng, lat);
    map.value?.setCenter([lng, lat]);
    map.value?.setFitView();

    const result = await gdMapUtil.GeocoderGetAddress(lng, lat);
    if (result.regeocode) {
      const locationData = await parseAddress(result, lng, lat, val.name);
      emit("select-location", locationData);
      emit("close-drawer");
    }
    addressOptions.value = [];
  } catch (error) {
    ElMessage.error(t("address.setLocationFailed"));
  }
};

// 获取当前位置
const getCurrentLocation = async () => {
  try {
    // 先检查权限状态
    if (navigator.permissions) {
      const permission = await navigator.permissions.query({ name: "geolocation" });
      if (permission.state === "denied") {
        ElMessage.warning(t("address.enableGeoPermission"));
        return;
      }
    }

    const result = await gdMapUtil.getCurrentPosition();
    if (!result?.position) {
      throw new Error(t("address.invalidPosition"));
    }

    // 修改这里：确保 position 是正确的格式
    // result.position 可能直接是一个对象而不是数组
    let lng: number, lat: number;
    if (Array.isArray(result.position)) {
      [lng, lat] = result.position;
    } else {
      lng = result.position.lng;
      lat = result.position.lat;
    }
    mapCenter.value = [lng, lat];

    if (map.value) {
      map.value.setCenter([lng, lat]);
      map.value.setZoom(17);
    }

    const geocodeResult = await gdMapUtil.GeocoderGetAddress(lng, lat);
    if (geocodeResult.regeocode) {
      // 使用统一的parseAddress方法，会自动搜索附近POI
      const locationData = await parseAddress(geocodeResult, lng, lat);
      targetAddress.value = locationData;
      setMark(lng, lat);
      emit("select-location", locationData);
    }
  } catch (error) {
    console.error("定位错误:", error);
    try {
      // IP 定位兜底
      const cityResult = await gdMapUtil.getLocationCity();
      // 修改这里：确保正确访问位置信息
      const lng = cityResult.position?.[0] ?? 116.397428;
      const lat = cityResult.position?.[1] ?? 39.90923;

      mapCenter.value = [lng, lat];
      if (map.value) {
        map.value.setCenter([lng, lat]);
        map.value.setZoom(12);
      }
    } catch (ipError) {
      console.error("IP定位失败:", ipError);
      ElMessage.warning(t("address.manualPickAddress"));
      // 使用默认坐标（北京）
      mapCenter.value = [116.397428, 39.90923];
      if (map.value) {
        map.value.setCenter(mapCenter.value);
        map.value.setZoom(12);
      }
    }
  }
};

// 生命周期
onMounted(async () => {
  loading.value = true;
  try {
    await getMap(); // 先初始化地图
    await getCurrentLocation(); // 然后再定位并设置中心点
  } catch (error) {
    console.error("地图加载异常:", error);
    ElMessage.error(t("address.mapLoadFailed"));
  } finally {
    loading.value = false;
  }
});

onBeforeUnmount(() => {
  if (map.value) {
    map.value.off("click", handleMapClick);
    gdMapUtil.destroyMap();
  }
});

const emit = defineEmits<{
  (e: "close-drawer"): void;
  (e: "select-location", data: LocationData): void;
}>();
</script>

<style scoped lang="less">
#guide-map {
  width: 100%;
  height: 100%;
}
</style>