package app.rafiqaldhikr

import android.os.Bundle
import androidx.activity.compose.setContent
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.rafiqaldhikr.ui.navigation.RafiqBottomBar
import app.rafiqaldhikr.ui.navigation.RafiqNavGraph
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.screens.settings.SettingsViewModel
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep splash while prefs haven't loaded yet
        splash.setKeepOnScreenCondition {
            settingsViewModel.onboardingCompleted.value == null
        }

        /*  تقريرُ الانهيار يسبق كلَّ شيء.
         *
         *  ويُقرأ **قبل** أوّل لمسةٍ لـ`settingsViewModel`: ذاك يُحلّ من
         *  Koin، فلو كان العطبُ في التهيئة نفسِها لانهارت شاشةُ التقرير
         *  قبل أن تُظهر سببَ الانهيار — وهو الشيءُ الوحيد المطلوب منها.
         *
         *  فما دام ثمّة تقريرٌ لم يُقرأ، لا يُشغَّل التطبيق أصلاً: يُعرض
         *  النصُّ، ولصاحبه أن يرسله أو يتجاوزه. */
        val crash = app.rafiqaldhikr.util.CrashLog.read(this)
        if (crash != null) {
            setContent { app.rafiqaldhikr.ui.screens.crash.CrashReportScreen(crash) }
            return
        }

        setContent {
            val onboardingCompleted by settingsViewModel.onboardingCompleted
                .collectAsStateWithLifecycle()
            val dynamicColor by settingsViewModel.dynamicColor
                .collectAsStateWithLifecycle()
            val themePref by settingsViewModel.theme
                .collectAsStateWithLifecycle()
            val arabicNumerals by settingsViewModel.arabicNumerals
                .collectAsStateWithLifecycle()
            val reducedMotionPref by settingsViewModel.reducedMotion
                .collectAsStateWithLifecycle()
            val fontScale by settingsViewModel.fontScale
                .collectAsStateWithLifecycle()
            val highContrast by settingsViewModel.highContrast
                .collectAsStateWithLifecycle()

            // Don't render until we know onboarding state
            if (onboardingCompleted == null) return@setContent

            val darkTheme = when (themePref) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }

            /*  أيقوناتُ شريطَي النظام تتبع اختيار المستخدم لا النظام.
             *
             *  `enableEdgeToEdge()` كان يُنادى مرّةً في `onCreate` بلا وسائط،
             *  فيشتقّ لونَ الأيقونات من ثيم **الجهاز**. ومن اختار «داكن»
             *  داخل التطبيق وهاتفُه فاتح كان يرى أيقوناتٍ داكنةً على شريطٍ
             *  داكن — أي شريطَ حالةٍ فارغاً بلا ساعةٍ ولا بطارية. والعكسُ
             *  كذلك.  */
            LaunchedEffect(darkTheme) {
                val style = if (darkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            /*  مقياسُ الخطّ يُطبَّق على التطبيق كلِّه.
             *
             *  كان الشريطُ في شاشة الخطّ يُحرّك سطرَ المعاينة وحدَه: القيمةُ
             *  تُحفظ في القاعدة، ولا يقرؤها `RafiqTheme` ولا `MainActivity`
             *  ولا شاشةٌ أخرى. فيحرّكه المستخدم، ويرى المعاينةَ تكبر، ثمّ
             *  يخرج فلا يتغيّر حرفٌ واحد.
             *
             *  والتطبيقُ على `fontScale` في الكثافة لا على كل `sp` مكتوبة:
             *  فيتبعه كلُّ نصٍّ في التطبيق بلا استثناء. ويُضرب في مقياس
             *  النظام لا يحلّ محلَّه — فمن كبّر الخطَّ في إعدادات هاتفه
             *  لأنّه يحتاجه، لا يُلغى عليه ذلك.  */
            val density = LocalDensity.current
            val scaledDensity = remember(density, fontScale) {
                Density(density.density, density.fontScale * fontScale)
            }

            CompositionLocalProvider(LocalDensity provides scaledDensity) {
            RafiqTheme(
                darkTheme    = darkTheme,
                dynamicColor = dynamicColor,
                highContrast = highContrast,
            ) {
              androidx.compose.runtime.CompositionLocalProvider(
                  app.rafiqaldhikr.ui.utils.LocalArabicNumerals provides arabicNumerals,
                  // إعداد «تقليل الحركة» كان موجوداً ولا يقرؤه أيّ أنيميشن.
                  // الآن يُقرأ من إعداد المستخدم ومن إعداد النظام معاً.
                  app.rafiqaldhikr.ui.theme.LocalReducedMotion provides
                      app.rafiqaldhikr.ui.theme.rememberReducedMotion(reducedMotionPref),
              ) {
               // طبقة الميقات: تحسب مواقيت اليوم مرّة واحدة، فتصبغ الورق
               // بضوء الوقت وتغذّي شريط الميقات في كل شاشة.
               app.rafiqaldhikr.ui.components.ProvideMeeqat {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                /*  شاشةٌ قد تطلب الورقةَ خالصة (المصحف). وتُصفَّر عند كل
                 *  انتقالٍ حتى لا تحمل شاشةٌ أثرَ ما قبلها فتفقد شريطَها.  */
                val immersive = remember { mutableStateOf(false) }
                LaunchedEffect(currentRoute) { immersive.value = false }

                // Bottom bar visible except on onboarding and celebration
                val showBottomBar = currentRoute !in listOf(
                    RafiqRoute.Onboarding.route,
                    RafiqRoute.Celebration.route,
                    RafiqRoute.DhikrReading.route,
                ) && !immersive.value

                Scaffold(
                    containerColor = LocalRafiqColors.current.bg,
                    // الشاشات تتكفل بـ statusBarsPadding بنفسها — منع ازدواج الحشوة
                    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        androidx.compose.foundation.layout.Column {
                            // حُذف شريط "لا يوجد اتصال": التطبيق لم يعد يفتح
                            // اتصالاً واحداً، فتحذيرُ انقطاعِ شبكةٍ لا يحتاجها
                            // يقول للمستخدم إن شيئاً معطَّل وليس كذلك.
                            if (showBottomBar) {
                                RafiqBottomBar(navController)
                            }
                        }
                    }
                ) { innerPadding ->
                    CompositionLocalProvider(
                        app.rafiqaldhikr.ui.navigation.LocalImmersive provides immersive
                    ) {
                        RafiqNavGraph(
                            navController       = navController,
                            onboardingCompleted = onboardingCompleted!!,
                            modifier            = Modifier.padding(innerPadding)
                        )
                    }
                }
               }
              }
            }
            }
        }
    }
}
