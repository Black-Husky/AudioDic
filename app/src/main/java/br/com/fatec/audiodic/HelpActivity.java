package br.com.fatec.audiodic;

import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HelpActivity extends ModelActivity {

    //Setting Fields
    private LinearLayout linearLayoutMain;
    private TextView textViewInfo;
    //End of Setting Fields

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        //Getting fields
        textViewInfo = (TextView) findViewById(R.id.textViewInfo);
        //End of getting fields

        String text1 = "<p><big>Como pesquisar por voz?</big><br/><br/>" +
                "Em qualquer tela, precione-a por mais ou menos 1 segundo até ouvir um som, não solte a tela e fale o comando que deseja, em seguida solte para o aplicativo executá-lo.<br/>" +
                "Para parar o retorno de voz, precione a tela por mais ou menos 1 segundo até ouvir um som e solte-a</p>";
        String text2 = "<p><big>Comandos por Voz Disponíveis</big><br/><br/>" +
                "<strong>- Pesquisar \"palavra\" : </strong>" +
                "Pesquisa a palavra solicitada<br/>" +
                "<strong>- Ler Definições : </strong>" +
                "Lê a definição da palavra pesquisada;<br/>" +
                "<strong>- Cancelar : </strong>" +
                "Cancela a pesquisa;</br></p>" +
                "<strong>- Comandos : </strong>" +
                "Informa os comandos ofericidos pelo aplicativo.";
        String text3 = "<p><big>Como pesquisar por texto?</big><br/><br/><strong>Tela Inicial</strong><br/>Para digitar a palavra, clique na caixa de texto que aparece no meio da tela;<br/> Após digitar a palavra, clique no botão pesquisar!<br/><br/><strong>Tela de Resultados</strong><br/>Clique na Lupa no canto superior direito e repita o precesso citado no parágrafo anterior.</p>";
        String finalText = text1+text2+text3;
        textViewInfo.setText(Html.fromHtml(finalText));

        linearLayoutMain = (LinearLayout) findViewById(R.id.linearLayoutMain);
        startVoiceService(linearLayoutMain);
    }
}
