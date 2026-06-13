pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // فرض الإدارة المركزية ومنع الموديلات الفرعية من إعلان مستودعات خاصة بها
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    
    repositories {
        // 1. مستودع Google الرسمي - محكوم بفلترة صارمة للحزم الأساسية لأندرويد
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        
        // 2. مستودع Maven Central - منع البحث عن حزم أندرويد الرسمية داخله
        mavenCentral {
            content {
                // استبعاد حزم أندرويد الرسمية من البحث هنا لتسريع الأداء
                excludeGroupByRegex("com\\.android.*")
                excludeGroupByRegex("androidx.*")
            }
        }
        
        // 3. مستودع JitPack - مؤمن بالكامل ومحصور فقط في المكتبات التي تحتاجها فعلياً
        maven { 
            url = uri("https://jitpack.io") 
            content {
                // تصفية صارمة: لا يسمح لـ JitPack بتقديم أي حزم باستثناء المحددة هنا
                // استبدل أو أضف الـ Group الخاص بالمكتبات التي تستخدمها من GitHub (مثال: مكتبات واجهة أو معالجة نصوص)
                includeGroup("com.github.bmelnychuk") // مثال لمكتبة تفرع شجري
                includeGroupByRegex("com\\.github\\.PhilJay.*") // مثال لمكتبة رسوم بيانية
                
                // يمكنك إضافة المجموعات الخاصة بمشروعك إذا كنت تجلب مكاتب مخصصة
            }
        }
    }
}

// إعدادات البنية المعمارية للمشروع الموحد (Agora)
rootProject.name = "Agora"

// الموديل الأساسي للتطبيق
include(":app")

// =============================================================================
// تفعيل ميزات Gradle المتقدمة للتطوير المستقبلي والـ Multi-Module (جاهزة للتفعيل)
// =============================================================================
// enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS") // لتسهيل استدعاء الموديلات كـ projects.app بدلاً من السلاسل النصية
