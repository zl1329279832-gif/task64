package com.entity.view;

import com.entity.MingpianHistoryEntity;
import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 名片版本历史
 * 后端返回视图实体辅助类
 */
@TableName("mingpian_history")
public class MingpianHistoryView extends MingpianHistoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

		/**
		* 性别的值
		*/
		private String sexValue;
		/**
		* 职务的值
		*/
		private String zhiwuValue;
		/**
		* 名片类型的值
		*/
		private String mingpianValue;
		/**
		* 学院的值
		*/
		private String xueyuanValue;
		/**
		* 办公室的值
		*/
		private String bangongshiValue;
		/**
		* 主修课程的值
		*/
		private String kechengValue;
		/**
		* 审批状态的值
		*/
		private String shangxiaValue;



		//级联表 jiaoshi
			/**
			* 教师编号
			*/
			private String jiaoshiUuidNumber;
			/**
			* 教师姓名
			*/
			private String jiaoshiName;
			/**
			* 教师手机号
			*/
			private String jiaoshiPhone;
			/**
			* 教师头像
			*/
			private String jiaoshiPhoto;

	public MingpianHistoryView() {

	}

	public MingpianHistoryView(MingpianHistoryEntity mingpianHistoryEntity) {
		try {
			BeanUtils.copyProperties(this, mingpianHistoryEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}



			/**
			* 获取： 性别的值
			*/
			public String getSexValue() {
				return sexValue;
			}
			/**
			* 设置： 性别的值
			*/
			public void setSexValue(String sexValue) {
				this.sexValue = sexValue;
			}
			/**
			* 获取： 职务的值
			*/
			public String getZhiwuValue() {
				return zhiwuValue;
			}
			/**
			* 设置： 职务的值
			*/
			public void setZhiwuValue(String zhiwuValue) {
				this.zhiwuValue = zhiwuValue;
			}
			/**
			* 获取： 名片类型的值
			*/
			public String getMingpianValue() {
				return mingpianValue;
			}
			/**
			* 设置： 名片类型的值
			*/
			public void setMingpianValue(String mingpianValue) {
				this.mingpianValue = mingpianValue;
			}
			/**
			* 获取： 学院的值
			*/
			public String getXueyuanValue() {
				return xueyuanValue;
			}
			/**
			* 设置： 学院的值
			*/
			public void setXueyuanValue(String xueyuanValue) {
				this.xueyuanValue = xueyuanValue;
			}
			/**
			* 获取： 办公室的值
			*/
			public String getBangongshiValue() {
				return bangongshiValue;
			}
			/**
			* 设置： 办公室的值
			*/
			public void setBangongshiValue(String bangongshiValue) {
				this.bangongshiValue = bangongshiValue;
			}
			/**
			* 获取： 主修课程的值
			*/
			public String getKechengValue() {
				return kechengValue;
			}
			/**
			* 设置： 主修课程的值
			*/
			public void setKechengValue(String kechengValue) {
				this.kechengValue = kechengValue;
			}
			/**
			* 获取： 审批状态的值
			*/
			public String getShangxiaValue() {
				return shangxiaValue;
			}
			/**
			* 设置： 审批状态的值
			*/
			public void setShangxiaValue(String shangxiaValue) {
				this.shangxiaValue = shangxiaValue;
			}




			//级联表的get和set jiaoshi

				/**
				* 获取： 教师编号
				*/
				public String getJiaoshiUuidNumber() {
					return jiaoshiUuidNumber;
				}
				/**
				* 设置： 教师编号
				*/
				public void setJiaoshiUuidNumber(String jiaoshiUuidNumber) {
					this.jiaoshiUuidNumber = jiaoshiUuidNumber;
				}

				/**
				* 获取： 教师姓名
				*/
				public String getJiaoshiName() {
					return jiaoshiName;
				}
				/**
				* 设置： 教师姓名
				*/
				public void setJiaoshiName(String jiaoshiName) {
					this.jiaoshiName = jiaoshiName;
				}

				/**
				* 获取： 教师手机号
				*/
				public String getJiaoshiPhone() {
					return jiaoshiPhone;
				}
				/**
				* 设置： 教师手机号
				*/
				public void setJiaoshiPhone(String jiaoshiPhone) {
					this.jiaoshiPhone = jiaoshiPhone;
				}

				/**
				* 获取： 教师头像
				*/
				public String getJiaoshiPhoto() {
					return jiaoshiPhoto;
				}
				/**
				* 设置： 教师头像
				*/
				public void setJiaoshiPhoto(String jiaoshiPhoto) {
					this.jiaoshiPhoto = jiaoshiPhoto;
				}


}
