package org.jetbrains.dokka

import ru.yole.jkid.CustomSerializer
import ru.yole.jkid.ValueSerializer
import ru.yole.jkid.deserialization.JKidException
import java.io.Serializable
import java.net.URL


class UrlSerializer : ValueSerializer<URL?> {
    override fun fromJsonValue(jsonValue: Any?): URL? {
        if (jsonValue !is String?)
            throw JKidException("Expected string representation of URL, got: $jsonValue")
        return jsonValue?.let { URL(jsonValue) }
    }

    override fun toJsonValue(value: URL?): Any? = value?.toExternalForm()
}

enum class Platform(val key: String) {
    jvm("jvm"),
    js("js"),
    common("common");


    companion object {
        val DEFAULT = jvm

        fun fromString(key: String): Platform {
            return when (key.toLowerCase()) {
                jvm.key -> jvm
                js.key -> js
                common.key -> common
                else -> TODO("write normal exception")
            }
        }
    }

}

interface DokkaConfiguration {
    val outputDir: String
    val format: String
    val generateIndexPages: Boolean
    val cacheRoot: String?
    val passesConfigurations: List<PassConfiguration>
    val impliedPlatforms: List<String>

    interface PassConfiguration {
        val moduleName: String
        val classpath: List<String>
        val sourceRoots: List<SourceRoot>
        val samples: List<String>
        val includes: List<String>
        val includeNonPublic: Boolean
        val includeRootPackage: Boolean
        val reportUndocumented: Boolean
        val skipEmptyPackages: Boolean
        val skipDeprecated: Boolean
        val jdkVersion: Int
        val sourceLinks: List<SourceLinkDefinition>
        val perPackageOptions: List<PackageOptions>
        val externalDocumentationLinks: List<DokkaConfiguration.ExternalDocumentationLink>
        val languageVersion: String?
        val apiVersion: String?
        val noStdlibLink: Boolean
        val noJdkLink: Boolean
        val suppressedFiles: List<String>
        val collectInheritedExtensionsFromLibraries: Boolean
        val analysisPlatform: Platform
        val targets: List<String>
    }

    interface SourceRoot {
        val path: String
        val platforms: List<String>
        val analysisPlatform: Platform
    }

    interface SourceLinkDefinition {
        val path: String
        val url: String
        val lineSuffix: String?
    }

    interface PackageOptions {
        val prefix: String
        val includeNonPublic: Boolean
        val reportUndocumented: Boolean
        val skipDeprecated: Boolean
        val suppress: Boolean
    }

    interface ExternalDocumentationLink {
        @CustomSerializer(UrlSerializer::class) val url: URL
        @CustomSerializer(UrlSerializer::class) val packageListUrl: URL

        open class Builder(open var url: URL? = null,
                           open var packageListUrl: URL? = null) {

            constructor(root: String, packageList: String? = null) : this(URL(root), packageList?.let { URL(it) })

            fun build(): DokkaConfiguration.ExternalDocumentationLink =
                    if (packageListUrl != null && url != null)
                        ExternalDocumentationLinkImpl(url!!, packageListUrl!!)
                    else if (url != null)
                        ExternalDocumentationLinkImpl(url!!, URL(url!!, "package-list"))
                    else
                        throw IllegalArgumentException("url or url && packageListUrl must not be null for external documentation link")
        }
    }
}

data class SerializeOnlyDokkaConfiguration(
    override val outputDir: String,
    override val format: String,
    override val generateIndexPages: Boolean,
    override val cacheRoot: String?,
    override val impliedPlatforms: List<String>,
    override val passesConfigurations: List<DokkaConfiguration.PassConfiguration>
) : DokkaConfiguration


data class ExternalDocumentationLinkImpl(@CustomSerializer(UrlSerializer::class) override val url: URL,
                                         @CustomSerializer(UrlSerializer::class) override val packageListUrl: URL) : Serializable, DokkaConfiguration.ExternalDocumentationLink