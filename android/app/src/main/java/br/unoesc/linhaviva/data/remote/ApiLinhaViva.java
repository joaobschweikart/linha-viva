package br.unoesc.linhaviva.data.remote;

import java.util.List;

import br.unoesc.linhaviva.data.remote.dto.AvisoDto;
import br.unoesc.linhaviva.data.remote.dto.EnvelopeDto;
import br.unoesc.linhaviva.data.remote.dto.HorarioDto;
import br.unoesc.linhaviva.data.remote.dto.InformacaoDto;
import br.unoesc.linhaviva.data.remote.dto.ItinerarioDto;
import br.unoesc.linhaviva.data.remote.dto.LinhaDto;
import br.unoesc.linhaviva.data.remote.dto.PontoDto;
import br.unoesc.linhaviva.data.remote.dto.PrevisaoDto;
import br.unoesc.linhaviva.data.remote.dto.PrevisaoLinhaDto;
import br.unoesc.linhaviva.data.remote.dto.VeiculoDto;
import br.unoesc.linhaviva.data.remote.dto.VersaoDto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiLinhaViva {

    @GET("versao")
    Call<VersaoDto> versao();

    @GET("itinerarios")
    Call<EnvelopeDto<List<ItinerarioDto>>> itinerarios();

    @GET("horarios")
    Call<EnvelopeDto<List<HorarioDto>>> horarios();

    @GET("linhas")
    Call<EnvelopeDto<List<LinhaDto>>> linhas();

    @GET("linhas/previsoes")
    Call<EnvelopeDto<List<PrevisaoLinhaDto>>> previsoesDasLinhas();

    @GET("linhas/{id}/itinerario")
    Call<EnvelopeDto<List<ItinerarioDto>>> itinerario(@Path("id") String linhaId,
                                                      @Query("sentido") String sentido);

    @GET("linhas/{id}/horarios")
    Call<EnvelopeDto<List<HorarioDto>>> horarios(@Path("id") String linhaId,
                                                 @Query("sentido") String sentido);

    @GET("linhas/{id}/veiculo")
    Call<EnvelopeDto<VeiculoDto>> veiculo(@Path("id") String linhaId,
                                          @Query("sentido") String sentido);

    @GET("pontos")
    Call<EnvelopeDto<List<PontoDto>>> pontos();

    @GET("pontos")
    Call<EnvelopeDto<List<PontoDto>>> pontosProximos(@Query("lat") double latitude,
                                                     @Query("lon") double longitude,
                                                     @Query("raio") int raioMetros,
                                                     @Query("limite") int limite);

    @GET("pontos/{id}/previsoes")
    Call<EnvelopeDto<List<PrevisaoDto>>> previsoesDoPonto(@Path("id") String pontoId,
                                                          @Query("limite") int limite);

    @GET("avisos")
    Call<EnvelopeDto<List<AvisoDto>>> avisos();

    @GET("informacoes")
    Call<EnvelopeDto<List<InformacaoDto>>> informacoes();
}
