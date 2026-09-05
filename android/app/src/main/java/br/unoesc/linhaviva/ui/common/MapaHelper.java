package br.unoesc.linhaviva.ui.common;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;

import br.unoesc.linhaviva.R;
import br.unoesc.linhaviva.util.GeoUtil;

/**
 * Configuracao do mapa (OpenStreetMap via osmdroid). Nao exige chave de API e
 * mantem um cache de tiles em disco, o que sustenta o uso offline.
 */
public final class MapaHelper {

    public static final GeoPoint CENTRO_CHAPECO =
            new GeoPoint(GeoUtil.CHAPECO_LAT, GeoUtil.CHAPECO_LON);

    private MapaHelper() {
    }

    public static void configurar(MapView mapa, double zoomInicial) {
        mapa.setTileSource(TileSourceFactory.MAPNIK);
        mapa.setMultiTouchControls(true);
        mapa.setTilesScaledToDpi(true);
        mapa.setFlingEnabled(true);
        mapa.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapa.setMinZoomLevel(11.0);
        mapa.setMaxZoomLevel(19.0);
        mapa.setHorizontalMapRepetitionEnabled(false);
        mapa.setVerticalMapRepetitionEnabled(false);
        mapa.getController().setZoom(zoomInicial);
        mapa.getController().setCenter(CENTRO_CHAPECO);
    }

    /** Mapa apenas ilustrativo: sem gestos, para nao competir com a rolagem da tela. */
    public static void configurarEstatico(MapView mapa, double zoomInicial) {
        configurar(mapa, zoomInicial);
        mapa.setMultiTouchControls(false);
        mapa.setFlingEnabled(false);
        mapa.setClickable(false);
        mapa.setEnabled(false);
    }

    public static Marker marcador(MapView mapa, GeoPoint posicao, @DrawableRes int icone,
                                  String titulo, Object referencia) {
        Marker marcador = new Marker(mapa);
        marcador.setPosition(posicao);
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marcador.setIcon(desenho(mapa.getContext(), icone));
        marcador.setTitle(titulo);
        marcador.setRelatedObject(referencia);
        marcador.setInfoWindow(null);
        return marcador;
    }

    public static Polyline tracado(Context contexto, List<GeoPoint> pontos, boolean destaque) {
        Polyline linha = new Polyline();
        linha.setPoints(pontos);
        linha.getOutlinePaint().setStrokeWidth(destaque ? 12f : 7f);
        linha.getOutlinePaint().setColor(ContextCompat.getColor(contexto,
                destaque ? R.color.azul_primario : R.color.azul_medio));
        linha.getOutlinePaint().setAlpha(destaque ? 255 : 150);
        linha.setInfoWindow(null);
        return linha;
    }

    public static void enquadrar(MapView mapa, List<GeoPoint> pontos, int margemPx) {
        if (pontos == null || pontos.isEmpty()) return;
        if (pontos.size() == 1) {
            mapa.getController().setZoom(16.5);
            mapa.getController().setCenter(pontos.get(0));
            return;
        }
        mapa.post(() -> {
            try {
                mapa.zoomToBoundingBox(BoundingBox.fromGeoPoints(pontos), false, margemPx);
            } catch (RuntimeException ignorado) {
                mapa.getController().setCenter(pontos.get(0));
            }
        });
    }

    private static Drawable desenho(Context contexto, @DrawableRes int recurso) {
        return ContextCompat.getDrawable(contexto, recurso);
    }
}
