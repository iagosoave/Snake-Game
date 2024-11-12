        package snakegame;

import java.awt.Color;

public class Quadrado {
    int x, y, largura, altura;
    Color cor;

    public Quadrado(int x, int y, Color cor) {
        this.x = x;
        this.y = y;
        this.largura = 10;
        this.altura = 10;
        this.cor = cor;
    }
}