package com.orion.atena.data.model

import com.google.gson.annotations.SerializedName

data class QuantumBenchmarkResponse(
    @SerializedName("status") val status: String,
    @SerializedName("engine") val engine: String,
    @SerializedName("qubits_processados") val qubitsProcessados: Int,
    @SerializedName("shots_executados") val shotsExecutados: Int,
    @SerializedName("tempo_execucao_ms") val tempoExecucaoMs: Double,
    @SerializedName("amostra_resultado") val amostraResultado: Map<String, Int>?,
    @SerializedName("ambiente") val ambiente: String?,
    @SerializedName("espaco_de_estados_hilbert") val espacoHilbert: String?,
    @SerializedName("motivo") val motivo: String?
)
