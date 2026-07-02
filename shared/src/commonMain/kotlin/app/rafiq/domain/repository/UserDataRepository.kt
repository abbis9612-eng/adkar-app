package app.rafiq.domain.repository

/**
 * إدارة بيانات المستخدم: التصدير الكامل كـ JSON والحذف النهائي.
 * لا يمس المحتوى الشرعي المضمّن (القرآن، الأذكار، الأدعية) — فقط بيانات الاستخدام.
 */
interface UserDataRepository {
    suspend fun exportAsJson(): String
    suspend fun clearAllUserData()
}
