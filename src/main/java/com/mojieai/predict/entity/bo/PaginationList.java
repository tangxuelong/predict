package com.mojieai.predict.entity.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

@Data
@NoArgsConstructor
public class PaginationList<T> implements Serializable, Iterable<T> {
    private static final long serialVersionUID = -6059628280162549106L;

    private PaginationInfo paginationInfo = null;
    private List<T> list = new ArrayList<>();

    public T get(int index) {
        return this.list.get(index);
    }

    public boolean addAll(Collection<? extends T> list) {
        return this.list.addAll(list);
    }

    public int size() {
        return this.list.size();
    }

    public boolean add(T e) {
        return this.list.add(e);
    }

    @Override
    public Iterator<T> iterator() {
        return this.list.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        this.list.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return this.list.spliterator();
    }

    public PaginationList(PaginationInfo paginationInfo, List<T> list){
        this.paginationInfo = paginationInfo;
        this.list = list;
    }
}
