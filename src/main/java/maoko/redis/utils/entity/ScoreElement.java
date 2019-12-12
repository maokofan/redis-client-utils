package maoko.redis.utils.entity;

/**
 * 排序元素
 */
public class ScoreElement {
    private double Socre;
    private String value;

    public ScoreElement(double Socre, String value) {
        this.Socre = Socre;
        this.value = value;
    }

    public double getSocre() {
        return Socre;
    }

    public String getValue() {
        return value;
    }

}
