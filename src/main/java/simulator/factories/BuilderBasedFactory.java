
package simulator.factories;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class BuilderBasedFactory<T> implements Factory<T> { // Factory basada en un conjunto de Builders, cada uno con su propio typeTag y datos específicos
    private Map<String, Builder<T>> builders;
    private List<JSONObject> buildersInfo;

    public BuilderBasedFactory() { // constructor sin parámetros, inicializa las estructuras de datos
        builders = new HashMap<>();
        buildersInfo = new LinkedList<>();
    }

    public BuilderBasedFactory(List<Builder<T>> builders) { // constructor que recibe una lista de Builders, llama al constructor sin parámetros y luego añade cada Builder a la fábrica
        this();
        for (Builder<T> b : builders) {
            addBuilder(b);
        }
    }

    public void addBuilder(Builder<T> b) { // método para añadir un Builder a la fábrica, lo añade al mapa de builders y también guarda su información en la lista de buildersInfo
        builders.put(b.getTypeTag(), b);
        buildersInfo.add(b.getInfo());
    }

    @Override
    public T createInstance(JSONObject info) { // método para crear una instancia de T a partir de un JSONObject, busca el Builder correspondiente al typeTag especificado en el JSONObject y utiliza ese Builder para crear la instancia
        if (info == null) {
            throw new IllegalArgumentException("'info' cannot be null");
        }

        Builder<T> builder = builders.get(info.getString("type"));

        if (builder != null) {
            JSONObject data = info.has("data") ? info.getJSONObject("data") : new JSONObject();
            T instance = builder.createInstance(data);
            if (instance != null) {
                return instance;
            }
        }

        throw new IllegalArgumentException("Unrecognized 'info': " + info.toString());
    }

    @Override
    public List<JSONObject> getInfo() {
        return Collections.unmodifiableList(buildersInfo);
    }
}