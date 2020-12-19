package space.nebulark.junisockets;

public interface ISignalingOperation<T> {
    ESignalingOperationCode opcode = null;

    default T data(T data) {
        return data;
    };
}
