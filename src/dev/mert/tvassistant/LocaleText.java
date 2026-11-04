package dev.mert.tvassistant;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Locale;

/** Small centralized UI-locale foundation. Internal tool IDs and command contracts remain unchanged. */
final class LocaleText {
  private LocaleText() {}

  static String locale(Context context) {
    SharedPreferences p = context.getSharedPreferences("assistant", Context.MODE_PRIVATE);
    String selected = p.getString("ui_locale", "auto");
    if (!"auto".equals(selected)) return selected;
    String language = Locale.getDefault().getLanguage();
    if ("es".equals(language)) return "es";
    if ("pt".equals(language)) return "pt-BR";
    return "en";
  }

  static String display(Context context) {
    String value = context.getSharedPreferences("assistant", Context.MODE_PRIVATE)
        .getString("ui_locale", "auto");
    if ("es".equals(value)) return "Español";
    if ("pt-BR".equals(value)) return "Português (Brasil)";
    if ("en".equals(value)) return "English";
    return "Automático (" + locale(context) + ")";
  }

  static String text(Context context, String source) {
    String lang = locale(context);
    if ("en".equals(lang)) return source;
    String[] row = null;
    for (String[] candidate : STRINGS) if (candidate[0].equals(source)) { row = candidate; break; }
    if (row == null) return source;
    return "es".equals(lang) ? row[1] : row[2];
  }

  static String toolLabel(Context context, String id) {
    String[] row = null;
    for (String[] candidate : TOOLS) if (candidate[0].equals(id)) { row = candidate; break; }
    if (row == null) return id;
    String lang = locale(context);
    return "en".equals(lang) ? id : "es".equals(lang) ? row[1] : row[2];
  }

  private static final String[][] STRINGS = {
    {"Chat","Conversar","Conversar"},{"History","Historial","Histórico"},{"Tools","Capacidades","Ferramentas"},
    {"Apps","Aplicaciones","Aplicativos"},{"Settings","Configuración","Configurações"},
    {"Speak","Hablar","Falar"},{"Say or type a command","Decí o escribí una instrucción","Diga ou digite um comando"},
    {"Run","Ejecutar","Executar"},{"Stop","Detener","Parar"},{"Ready","Listo","Pronto"},
    {"What would you like to do?","¿Qué te gustaría hacer?","O que você gostaria de fazer?"},
    {"Find a show","Buscar una película o serie","Encontrar filme ou série"},
    {"Browse the web","Navegar por Internet","Navegar na internet"},
    {"Device info","Información del dispositivo","Informações do dispositivo"},
    {"Weather","Clima","Clima"},{"Search a title in Stremio","Buscar un título en Stremio","Buscar título no Stremio"},
    {"Look something up on the web","Buscar algo en Internet","Pesquisar na internet"},
    {"Read this TV's details","Consultar los datos del televisor","Ver dados desta TV"},
    {"Current conditions and forecast","Condiciones actuales y pronóstico","Condições atuais e previsão"},
    {"Settings","Configuración","Configurações"},{"Language","Idioma","Idioma"},
    {"Automatic","Automático","Automático"},{"Close","Cerrar","Fechar"},
    {"History","Historial","Histórico"},{"Installed apps","Aplicaciones instaladas","Aplicativos instalados"},
    {"Choose an app to open.","Elegí una aplicación para abrir.","Escolha um aplicativo para abrir."},
    {"What the assistant can do. AI tasks are limited to the tools that match your request.","Qué puede hacer el asistente. Las tareas de IA se limitan a las herramientas relacionadas con tu pedido.","O que o assistente pode fazer. As tarefas de IA ficam limitadas às ferramentas relacionadas ao seu pedido."}
  };

  private static final String[][] TOOLS = {
    {"web_page","Consultar página web","Consultar página web"},{"action_plan","Plan de acciones","Plano de ações"},
    {"ui_target","Buscar elemento en pantalla","Localizar item na tela"},{"workflow","Flujos guardados","Fluxos salvos"},
    {"list_apps","Ver aplicaciones instaladas","Listar aplicativos instalados"},{"open_app","Abrir aplicación","Abrir aplicativo"},
    {"search_app","Buscar dentro de una aplicación","Pesquisar em um aplicativo"},{"open_url","Abrir enlace","Abrir link"},
    {"youtube_search","Buscar en YouTube","Pesquisar no YouTube"},{"youtube_latest","Últimos videos de un canal","Vídeos mais recentes do canal"},
    {"youtube_play","Reproducir video","Reproduzir vídeo"},{"web_search","Buscar en Internet","Pesquisar na internet"},
    {"find_media","Buscar película o serie","Encontrar filme ou série"},{"media_details","Ver detalles del contenido","Ver detalhes do conteúdo"},
    {"list_sessions","Ver sesiones multimedia","Listar sessões de mídia"},{"playback","Controlar reproducción","Controlar reprodução"},
    {"volume","Controlar volumen","Controlar volume"},{"open_settings","Abrir ajustes del dispositivo","Abrir configurações do dispositivo"},
    {"device_info","Información del dispositivo","Informações do dispositivo"},{"clock","Fecha y hora","Data e hora"},
    {"calculate","Calculadora","Calculadora"},{"weather","Clima","Clima"},{"notes","Notas","Notas"},
    {"preferences","Preferencias","Preferências"},{"routines","Rutinas","Rotinas"},{"timer","Temporizador","Temporizador"},
    {"screen_read","Leer pantalla","Ler tela"},{"screen_see","Ver pantalla","Visualizar tela"},
    {"screen_tap","Tocar coordenadas","Tocar coordenadas"},{"keyboard_keys","Enviar teclas","Enviar teclas"},
    {"screen_swipe","Deslizar en pantalla","Deslizar na tela"},{"screen_click","Pulsar elemento","Clicar em elemento"},
    {"screen_type","Escribir en pantalla","Digitar na tela"},{"screen_scroll","Desplazar pantalla","Rolar a tela"},
    {"navigate","Navegar por la interfaz","Navegar pela interface"},{"browser_read","Leer página abierta","Ler página aberta"},
    {"browser_action","Interactuar con página","Interagir com página"},{"browser_navigation","Navegar en el navegador","Navegar no navegador"},
    {"wait","Esperar una condición","Aguardar condição"},{"speak","Leer respuesta en voz alta","Ler resposta em voz alta"}
  };
}
