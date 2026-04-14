package com.tamdao.cinestream.core.util

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Maps technical exceptions to user-friendly UiText messages.
 */
object ErrorMapper {
    fun mapToUiText(throwable: Throwable): UiText {
        return when (throwable) {
            is UnknownHostException -> {
                UiText.DynamicString("Không có kết nối internet. Vui lòng kiểm tra lại mạng.")
            }
            is SocketTimeoutException -> {
                UiText.DynamicString("Yêu cầu quá hạn. Vui lòng thử lại sau.")
            }
            is IOException -> {
                UiText.DynamicString("Lỗi kết nối mạng. Vui lòng thử lại.")
            }
            is HttpException -> {
                when (throwable.code()) {
                    401 -> UiText.DynamicString("Phiên làm việc hết hạn. Vui lòng đăng nhập lại.")
                    403 -> UiText.DynamicString("Bạn không có quyền truy cập tính năng này.")
                    429 -> UiText.DynamicString("Hệ thống đang bận do quá nhiều yêu cầu. Vui lòng đợi một lát.")
                    500 -> UiText.DynamicString("Máy chủ đang gặp sự cố. Chúng tôi sẽ sớm khắc phục.")
                    503 -> UiText.DynamicString("Dịch vụ đang bảo trì. Vui lòng quay lại sau.")
                    else -> UiText.DynamicString("Có lỗi xảy ra (HTTP ${throwable.code()}). Vui lòng thử lại.")
                }
            }
            else -> {
                val message = throwable.message ?: "Lỗi không xác định"
                if (message.contains("HandshakeException") || message.contains("SSL")) {
                    UiText.DynamicString("Lỗi bảo mật kết nối. Vui lòng thử lại.")
                } else {
                    UiText.DynamicString("Đã có lỗi xảy ra. Vui lòng thử lại sau.")
                }
            }
        }
    }
}
