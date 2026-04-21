<template>
	<view :class="['main-list','oBorder', {'has-prefix-icon': !!prefixIcon}]">
		<!-- 左侧前缀图标（可选，使用 u-icon） -->
		<view v-if="prefixIcon" class="wl-prefix-icon">
			<u-icon :name="prefixIcon" color="#3d8af5" size="36rpx"></u-icon>
		</view>
		<!-- 文本框 -->
		<input 
			class="main-input" 
			:value="value" 
			:type="_type"
			:focus="_focus"
			:maxlength="maxlength" 
			:placeholder="placeholder" 
			:password="type==='password'&&!showPassword" 
			
			@input="$emit('input', $event.detail.value)"
			@blur="$emit('blur', $event)"
			@focus="$emit('focus', $event)"
			@longpress="$emit('longpress', $event)"
			@confirm="$emit('confirm', $event)"
			@click="$emit('click', $event)"
			@longtap="$emit('longtap', $event)"
			@touchcancel="$emit('touchcancel', $event)"
			@touchend="$emit('touchend', $event)"
			@touchmove="$emit('touchmove', $event)"
			@touchstart="$emit('touchstart', $event)"
		/>
		<!-- 是否可见密码（右侧眼睛图标，使用 u-icon） -->
		<view
			v-if="_isShowPass && type === 'password' && !_isShowCode"
			class="img"
			@tap="showPass"
		>
			<u-icon
				:name="showPassword ? 'eye-off' : 'eye'"
				color="#c0c4cc"
				size="32rpx"
			></u-icon>
		</view>
		<!-- 倒计时 -->
		<view 
			v-if="_isShowCode&&!_isShowPass"
			:class="['vercode',{'vercode-run': second>0}]" 
			@click="setCode"
		>{{ getVerCodeSecond }}</view>
		
	</view>
</template>

<script>
	import uIcon from '@/uni_modules/uview-ui/components/u-icon/u-icon.vue';
	let _this, countDown;
	export default{
		data(){
			return{
				showPassword: false, //是否显示明文
				second: 0, //倒计时
				isRunCode: false, //是否开始倒计时
			}
		},
		props:{
			type: String, //类型
			value: String, //值
			placeholder: String, //框内提示
			maxlength: {
				//最大长度
				type: [Number,String],
				default: 20,
			},
			isShowPass:{
				//是否显示密码图标（二选一）
				type: [Boolean,String],
				default: false,
			},
			isShowCode:{
				//是否显示获取验证码（二选一）
				type: [Boolean,String],
				default: false,
			},
			prefixIcon:{
				// 左侧图标 class，如 'icon-username' / 'icon-pwd'
				type: String,
				default: ''
			},
			codeText:{
				type: String,
				default: "获取验证码",
			},
			setTime:{
				//倒计时时间设置
				type: [Number,String],
				default: 60,
			},
			focus:{  
				//是否聚焦  
				type: [Boolean,String],  
				default: false  
			}  
		},
		components: {
			uIcon
		},
		model: {
			prop: 'value',
			event: 'input'
		},
		mounted() {
			_this=this
			//准备触发
			this.$on('runCode',(val)=>{
                this.runCode(val);
            });
			clearInterval(countDown);//先清理一次循环，避免缓存
		},
		methods:{
			showPass(){
				//是否显示密码
				this.showPassword = !this.showPassword
			},
			setCode(){
				//设置获取验证码的事件
				if(this.isRunCode){
					//判断是否开始倒计时，避免重复点击
					return false;
				}
				this.$emit('setCode')
			},
			runCode(val){
				//开始倒计时
				if(String(val)=="0"){
					
					//判断是否需要终止循环
					this.second = 0; //初始倒计时
					clearInterval(countDown);//清理循环
					this.isRunCode= false; //关闭循环状态
					return false;
				}
				if(this.isRunCode){
					//判断是否开始倒计时，避免重复点击
					return false;
				}
				this.isRunCode= true
				this.second = this._setTime //倒数秒数
				
				let _this=this;
				countDown = setInterval(function(){
					_this.second--
					if(_this.second==0){
						_this.isRunCode= false
						clearInterval(countDown)
					}
				},1000)
			}
		},
		computed:{
			_type(){
				//处理值
				const type = this.type
				return type == 'password' ? 'text' : type
			},
			_isShowPass() {
				//处理值
				return String(this.isShowPass) !== 'false'
			},
			_isShowCode() {
				//处理值
				return String(this.isShowCode) !== 'false'
			},
			_setTime() {
				//处理值
				const setTime = Number(this.setTime)
				return setTime>0 ? setTime : 60
			},
			_focus() {  
				//处理值  
				return String(this.focus) !== 'false'  
			},  
			getVerCodeSecond(){
				//验证码倒计时计算
				if(this.second<=0){
					return this.codeText;
				}else{
					if(this.second<10){
						return '0'+this.second;
					}else{
						return this.second;
					}
				}
				
			}
		}
	}
</script>

<style>
	@import url("./css/icon.css");
	
	.main-list{
		display: flex;
		flex-direction: row;
		justify-content: space-between;
		align-items: center;
		/* height: 36rpx; */   /* Input 高度 */
		color: #333333;
		padding: 40rpx 32rpx;
		margin:32rpx 0;
	}
	.img{
		width: 32rpx;
		height: 32rpx;
		font-size: 32rpx;
	}
	.wl-prefix-icon{
		width: 76rpx;
		height: 76rpx;
		border-radius: 50%;
		background-color: #f5f7fb;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	/* u-icon 已自带尺寸和颜色，这里无需再单独限定 .cuIcon */
	.main-input{
		flex: 1;
		text-align: left;
		font-size: 28rpx;
		/* line-height: 100rpx; */
		padding-right: 10rpx;
	}
	.has-prefix-icon .main-input{
		margin-left: 28rpx;
	}
	.main-list:not(.has-prefix-icon) .main-input{
		margin-left: 20rpx;
	}
	.vercode {
		color: #ffffff;
		background: #3d8af5;
		font-size: 24rpx;
		font-weight: 600;
		padding: 0 28rpx;
		height: 64rpx;
		line-height: 64rpx;
		border-radius: 999rpx;
		box-shadow: 0 8rpx 24rpx rgba(61,138,245,0.35);
	}
	.vercode-run {
		background: rgba(61,138,245,0.18) !important;
		color: #8bb5fb !important;
		box-shadow: none;
	}
	.oBorder{
	    border: none;
	    border-radius: 2.5rem ;
	    -webkit-box-shadow: 0 0 60rpx 0 rgba(43,86,112,.1) ;
	    box-shadow: 0 0 60rpx 0 rgba(43,86,112,.1) ;
	}
</style>
