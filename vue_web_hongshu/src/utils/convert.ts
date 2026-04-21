interface DateFormatObj {
  y: number;
  m: number;
  d: number;
  h: number;
  i: number;
  s: number;
  a: number;
}

interface ListItem {
  value: string | number;
  text: string;
}

export default {
  parseTime(time: Date | string | number, cFormat?: string): string | null {
    if (arguments.length === 0 || !time) {
      return null;
    }
    const format = cFormat || '{y}-{m}-{d} {h}:{i}:{s}';
    let date: Date;
    if (typeof time === 'object') {
      date = time;
    } else {
      if (typeof time === 'string') {
        if (/^[0-9]+$/.test(time)) {
          // support "1548221490638"
          time = parseInt(time);
        } else {
          // support safari
          // https://stackoverflow.com/questions/4310953/invalid-date-in-safari
          time = time.replace(new RegExp(/-/gm), '/');
        }
      }

      if (typeof time === 'number' && time.toString().length === 10) {
        time = time * 1000;
      }
      date = new Date(time);
    }
    const formatObj: DateFormatObj = {
      y: date.getFullYear(),
      m: date.getMonth() + 1,
      d: date.getDate(),
      h: date.getHours(),
      i: date.getMinutes(),
      s: date.getSeconds(),
      a: date.getDay(),
    };
    const time_str = format.replace(/{([ymdhisa])+}/g,  (_result, key: keyof DateFormatObj) => {
      const value = formatObj[key];
      // Note: getDay() returns 0 on Sunday
      if (key === 'a') {
        return ['日', '一', '二', '三', '四', '五', '六'][value];
      }
      return value.toString().padStart(2, '0');
    });
    return time_str;
  },

  /**
   * @param {number} time
   * @param {string} option
   * @returns {string}
   */
  formatTime(time: number | string, option?: string): string {
    if (('' + time).length === 10) {
      time = parseInt(time as string) * 1000;
    } else {
      time = +time;
    }
    const d = new Date(time);
    const now = Date.now();

    const diff = (now - d.getTime()) / 1000;

    if (diff < 30) {
      return '刚刚';
    } else if (diff < 3600) {
      // less 1 hour
      return Math.ceil(diff / 60) + '分钟前';
    } else if (diff < 3600 * 24) {
      return Math.ceil(diff / 3600) + '小时前';
    } else if (diff < 3600 * 24 * 2) {
      return '1天前';
    }
    if (option) {
      return this.parseTime(time, option) || '';
    } else {
      return (
        d.getMonth() +
        1 +
        '月' +
        d.getDate() +
        '日' +
        d.getHours() +
        '时' +
        d.getMinutes() +
        '分'
      );
    }
  },

  /**
   * @param {string} url
   * @returns {Object}
   */
  param2Obj(url: string): { [key: string]: string } {
    const search = decodeURIComponent(url.split('?')[1]).replace(/\+/g, ' ');
    if (!search) {
      return {};
    }
    const obj: { [key: string]: string } = {};
    const searchArr = search.split('&');
    searchArr.forEach((v) => {
      const index = v.indexOf('=');
      if (index !== -1) {
        const name = v.substring(0, index);
        const val = v.substring(index + 1, v.length);
        obj[name] = val;
      }
    });
    return obj;
  },

  to_price(x: string | number): string {
    let f = parseFloat(x as string);
    if (isNaN(f)) {
      return '0.00';
    }
    const f1 = parseFloat(x as string);
    f = Math.round(f1 * 100) / 100;
    let s = f.toString();
    let rs = s.indexOf('.');
    if (rs < 0) {
      rs = s.length;
      s += '.';
    }
    while (s.length <= rs + 2) {
      s += '0';
    }
    return s;
  },

  // list to text
  to_text(value: string | number, list: ListItem[]): string {
    for (let i = 0; i !== list.length; ++i) {
      if (list[i].value == value) {
        return list[i].text;
      }
    }
    return '';
  },
};