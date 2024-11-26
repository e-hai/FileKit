package com.kit.file.model

enum class VideoType(val type: String) {
    VIDEO_MPEG("video/mpeg"),
    VIDEO_AV1("video/av01"),
    VIDEO_AVI("video/x-msvideo"),
    VIDEO_DIVX("video/divx"),
    VIDEO_DOLBY_VISION("video/dolby-vision"),
    VIDEO_H263("video/3gpp"),
    VIDEO_H264("video/avc"),
    VIDEO_H265("video/hevc"),
    VIDEO_MJPEG("video/mjpeg"),
    VIDEO_MP2T("video/mp2t"),
    VIDEO_MP4("video/mp4"),
    VIDEO_MP42("video/mp42"),
    VIDEO_MP43("video/mp43"),
    VIDEO_MP4V("video/mp4v-es"),
    VIDEO_MPEG2("video/mpeg2"),
    VIDEO_OGG("video/ogg"),
    VIDEO_PS("video/mp2p"),
    VIDEO_VC1("video/wvc1"),
    VIDEO_WEBM("video/webm"),
}

enum class ImageType(val type: String) {
    IMAGE_JPEG("image/jpeg"),
    IMAGE_HEIC("image/heic"),
    IMAGE_HEIF("image/heif"),
    IMAGE_PNG("image/png"),
    IMAGE_WEBP("image/webp")
}

enum class AudioType(val type: String) {
    AUDIO_AAC("audio/mp4a-latm"),
    AUDIO_AC3("audio/ac3"),
    AUDIO_AC4("audio/ac4"),
    AUDIO_ALAC("audio/alac"),
    AUDIO_ALAW("audio/g711-alaw"),
    AUDIO_AMR("audio/amr"),
    AUDIO_AMR_NB("audio/3gpp"),
    AUDIO_AMR_WB("audio/amr-wb"),
    AUDIO_DTS("audio/vnd.dts"),
    AUDIO_DTS_EXPRESS("audio/vnd.dts.hd;profile=lbr"),
    AUDIO_DTS_HD("audio/vnd.dts.hd"),
    AUDIO_E_AC3("audio/eac3"),
    AUDIO_E_AC3_JOC("audio/eac3-joc"),
    AUDIO_FLAC("audio/flac"),
    AUDIO_MIDI("audio/midi"),
    AUDIO_MLAW("audio/g711-mlaw"),
    AUDIO_MP4("audio/mp4"),
    AUDIO_MPEG("audio/mpeg"),
    AUDIO_MPEGH_MHA1("audio/mha1"),
    AUDIO_MPEGH_MHM1("audio/mhm1"),
    AUDIO_MPEG_L1("audio/mpeg-L1"),
    AUDIO_MPEG_L2("audio/mpeg-L2"),
    AUDIO_MSGSM("audio/gsm"),
    AUDIO_OGG("audio/ogg"),
    AUDIO_OPUS("audio/opus"),
    AUDIO_RAW("audio/raw"),
    AUDIO_TRUEHD("audio/true-hd"),
    AUDIO_VORBIS("audio/vorbis"),
    AUDIO_WAV("audio/wav"),
    AUDIO_WEBM("audio/webm")
}