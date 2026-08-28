package com.basitce.gfx.core.core_engine

/**
 * Tüm config parser'ların uyması gereken ortak sözleşme.
 *
 * PMP mantığında parser'lar dosyayı doğrudan disk'te değiştirmez.
 * İçerik RAM üzerinde parse edilir, değiştirilir ve serialize edilir.
 */
interface ConfigParser {

    /**
     * Dosya içeriğini bellekte ayrıştırır.
     *
     * @param content Config dosyasının ham metin içeriği.
     * @throws ConfigParserException İçerik parse edilemezse fırlatılır.
     */
    fun parse(content: String)

    /**
     * Belirtilen path üzerindeki değeri günceller.
     *
     * JSON tarafında:
     * $.root.engine.fps
     *
     * XML tarafında:
     * CVars/r.PUBG.Quality
     * /Config/Graphics/@Quality
     * xpath://Config/Graphics/@Quality
     *
     * INI tarafında:
     * Section/Key
     *
     * @param path Config yolu.
     * @param value Yazılacak değer.
     */
    fun updateValue(path: String, value: Any)

    /**
     * Bellekteki güncel config'i tekrar string'e çevirir.
     */
    fun serialize(): String
}

/**
 * Parser katmanı genel hata sınıfı.
 */
open class ConfigParserException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Path bulunamadığında ve güvenli şekilde oluşturulamadığında fırlatılır.
 */
class ConfigPathNotFoundException(
    message: String,
    cause: Throwable? = null
) : ConfigParserException(message, cause)
