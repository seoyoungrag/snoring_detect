package snoring.scichart;

public interface IValues<T> extends ICleanable {
    Class<T> getValuesType();

    int size();
}
