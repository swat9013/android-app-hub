package io.github.swat9013.apphub

object DeepLink {

    /**
     * apphub://<package.name> 形式の URI 文字列から起動対象のパッケージ名を取り出す。
     * 対象が特定できない場合は null。
     */
    fun extractPackageName(dataString: String?): String? {
        val afterScheme = dataString
            ?.substringAfter("://", missingDelimiterValue = "")
            ?: return null
        return afterScheme
            .substringBefore('?')
            .trim('/')
            .substringBefore('/')
            .takeIf { it.isNotEmpty() }
    }
}
