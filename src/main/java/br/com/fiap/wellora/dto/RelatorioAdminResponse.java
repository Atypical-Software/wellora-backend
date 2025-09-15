package br.com.fiap.wellora.dto;

import java.util.List;
import java.util.Map;

public class RelatorioAdminResponse {
    private String titulo;
    private PesquisasInfo pesquisas;
    private List<SentimentoInfo> sentimentos;
    private ColaboradoresInfo colaboradoresComCansaco;

    public RelatorioAdminResponse() {}

    // Getters e Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public PesquisasInfo getPesquisas() { return pesquisas; }
    public void setPesquisas(PesquisasInfo pesquisas) { this.pesquisas = pesquisas; }

    public List<SentimentoInfo> getSentimentos() { return sentimentos; }
    public void setSentimentos(List<SentimentoInfo> sentimentos) { this.sentimentos = sentimentos; }

    public ColaboradoresInfo getColaboradoresComCansaco() { return colaboradoresComCansaco; }
    public void setColaboradoresComCansaco(ColaboradoresInfo colaboradoresComCansaco) { this.colaboradoresComCansaco = colaboradoresComCansaco; }

    // Classes internas
    public static class PesquisasInfo {
        private int concluidas;
        private int total;
        private int porcentagem;

        public PesquisasInfo() {}
        public PesquisasInfo(int concluidas, int total, int porcentagem) {
            this.concluidas = concluidas;
            this.total = total;
            this.porcentagem = porcentagem;
        }

        public int getConcluidas() { return concluidas; }
        public void setConcluidas(int concluidas) { this.concluidas = concluidas; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getPorcentagem() { return porcentagem; }
        public void setPorcentagem(int porcentagem) { this.porcentagem = porcentagem; }
    }

    public static class SentimentoInfo {
        private String tipo;
        private int quantidade;
        private int porcentagem;

        public SentimentoInfo() {}
        public SentimentoInfo(String tipo, int quantidade, int porcentagem) {
            this.tipo = tipo;
            this.quantidade = quantidade;
            this.porcentagem = porcentagem;
        }

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public int getQuantidade() { return quantidade; }
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
        public int getPorcentagem() { return porcentagem; }
        public void setPorcentagem(int porcentagem) { this.porcentagem = porcentagem; }
    }

    public static class ColaboradoresInfo {
        private String periodo;
        private int porcentagemCansado;
        private int porcentagemOk;

        public ColaboradoresInfo() {}
        public ColaboradoresInfo(String periodo, int porcentagemCansado, int porcentagemOk) {
            this.periodo = periodo;
            this.porcentagemCansado = porcentagemCansado;
            this.porcentagemOk = porcentagemOk;
        }

        public String getPeriodo() { return periodo; }
        public void setPeriodo(String periodo) { this.periodo = periodo; }
        public int getPorcentagemCansado() { return porcentagemCansado; }
        public void setPorcentagemCansado(int porcentagemCansado) { this.porcentagemCansado = porcentagemCansado; }
        public int getPorcentagemOk() { return porcentagemOk; }
        public void setPorcentagemOk(int porcentagemOk) { this.porcentagemOk = porcentagemOk; }
    }
}
