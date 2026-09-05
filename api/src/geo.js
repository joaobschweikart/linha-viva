'use strict';

const RAIO_TERRA_M = 6371000;

function distanciaMetros(lat1, lon1, lat2, lon2) {
  const rad = Math.PI / 180;
  const dLat = (lat2 - lat1) * rad;
  const dLon = (lon2 - lon1) * rad;
  const a = Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1 * rad) * Math.cos(lat2 * rad) * Math.sin(dLon / 2) ** 2;
  return Math.round(2 * RAIO_TERRA_M * Math.asin(Math.sqrt(a)));
}

function interpolar(a, b, fracao) {
  return {
    latitude: a.latitude + (b.latitude - a.latitude) * fracao,
    longitude: a.longitude + (b.longitude - a.longitude) * fracao
  };
}

module.exports = { distanciaMetros, interpolar };
