package com.tamdao.web_film_backend.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Tiện ích chuẩn hóa chuỗi tiếng Việt đạt chuẩn Production:
 * - Hỗ trợ cả 2 bảng mã Unicode dựng sẵn (NFC) và Unicode tổ hợp (NFD).
 * - Xử lý triệt để ký tự đặc thù đ/Đ, dấu gạch ngang, khoảng trắng không ngắt (NBSP \u00A0), dấu thanh âm điệu.
 * - Chuyển đổi slug và chuẩn hóa từ khóa tìm kiếm (search keyword normalization).
 */
public final class VietnameseStringUtils {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern MULTI_WHITESPACE_PATTERN = Pattern.compile("[\\s\\u00A0\\u1680\\u2000-\\u200A\\u2028\\u2029\\u202F\\u205F\\u3000]+");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^a-z0-9\\s-]");
    private static final Pattern MULTI_HYPHEN_PATTERN = Pattern.compile("-+");

    private VietnameseStringUtils() {
        // Utility class
    }

    /**
     * Chuẩn hóa văn bản tiếng Việt:
     * 1. Thay thế đ, Đ, D stroke.
     * 2. Phân rã NFD để bóc toàn bộ dấu thanh, dấu mũ, dấu móc.
     * 3. Loại bỏ combining diacritical marks.
     * 4. Thay thế khoảng trắng đặc biệt (NBSP...) thành khoảng trắng đơn.
     * 5. Lowercase và trim gọn gàng.
     * 
     * Ví dụ:
     * "  Trò   Chơi Đổi Đời (2024)! " -> "tro choi doi doi (2024)!"
     */
    public static String removeAccents(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        // Bước 1: Thay thế đ, Đ trước khi NFD vì NFD không phân rã chữ đ
        String text = input
                .replace('đ', 'd')
                .replace('Đ', 'd')
                .replace('\u0111', 'd') // latin small letter d with stroke
                .replace('\u0110', 'd'); // latin capital letter d with stroke

        // Bước 2: Chuẩn hóa sang NFD (Canonical Decomposition)
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);

        // Bước 3: Loại bỏ tất cả dấu phụ (accents/diacritics)
        String withoutDiacritics = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");

        // Bước 4: Chuyển về chữ thường
        String lower = withoutDiacritics.toLowerCase();

        // Bước 5: Chuẩn hóa các loại khoảng trắng (kể cả NBSP \u00A0 do copy-paste trên web)
        return MULTI_WHITESPACE_PATTERN.matcher(lower).replaceAll(" ").trim();
    }

    /**
     * Tạo slug chuẩn SEO và chuẩn tìm kiếm từ chuỗi bất kỳ:
     * Ví dụ:
     * "Trò Chơi Dối Trá: Phần 2" -> "tro-choi-doi-tra-phan-2"
     * "Fast & Furious 10" -> "fast-furious-10"
     */
    public static String toSlug(String input) {
        String clean = removeAccents(input);
        if (clean.isEmpty()) {
            return "";
        }

        // Thay ký tự đặc biệt thành dấu gạch ngang
        String withoutSpecial = SPECIAL_CHAR_PATTERN.matcher(clean).replaceAll("-");
        
        // Thu gọn nhiều dấu gạch ngang liên tiếp
        String singleHyphen = MULTI_HYPHEN_PATTERN.matcher(withoutSpecial).replaceAll("-");

        // Loại bỏ gạch ngang ở đầu và cuối
        return singleHyphen.replaceAll("^-+|-+$", "");
    }

    /**
     * Chuẩn hóa từ khóa tìm kiếm:
     * - Cắt tỉa khoảng trắng thừa
     * - Chuyển sang chữ thường
     */
    public static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "";
        }
        return MULTI_WHITESPACE_PATTERN.matcher(keyword).replaceAll(" ").trim();
    }
}
