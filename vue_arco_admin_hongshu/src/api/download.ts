import axios from 'axios';
import {Message} from '@arco-design/web-vue';
import {blobValidate, tansParams} from "@/utils/common";
import {saveAs} from 'file-saver';
import {getToken} from "@/utils/auth";
import errorCode from "@/utils/error-code";

// 通用下载方法
export function download(url: string, params: any, filename: string, config?: any) {
    return axios.post(url, params, {
        transformRequest: [(p) => tansParams(p)],
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        responseType: 'blob',
        ...config
    }).then(async (data) => {
        const isBlob = blobValidate(data);
        if (isBlob) {
            const blob = new Blob([data])
            saveAs(blob, filename)
        } else {
            const resText = await (data as any).text();
            const rspObj = JSON.parse(resText);
            const errMsg = (errorCode as any)[rspObj.code] || rspObj.msg || (errorCode as any).default
            Message.error(errMsg);
        }
    }).catch((r) => {
        console.error(r)
        Message.error('下载文件出现错误，请联系管理员！')
    })
}

export function downloadFile(url: string, params: any, filename: string, config?: any) {

}

async function printErrMsg(data: any) {
    const resText = await data.text();
    const rspObj = JSON.parse(resText);
    // @ts-ignore
    const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode.default
    Message.error(errMsg);
}

export function downloadZip(url: string, name: string) {
    const fullURL = url
    const token = getToken();
    axios({
        method: 'get',
        url: fullURL,
        responseType: 'blob',
        headers: { 'Authorization': `Bearer ${token}`  }
    }).then((data) => {
        const isBlob = blobValidate(data);
        if (isBlob) {
            const blob = new Blob([data], { type: 'application/zip' })
            saveAs(blob, name)
        } else {
            printErrMsg(data);
        }
    })
}
