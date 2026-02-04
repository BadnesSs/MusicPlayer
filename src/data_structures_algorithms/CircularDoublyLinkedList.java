package data_structures_algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

public class CircularDoublyLinkedList<T> {

    private final LinkedList<T> list = new LinkedList<>();



    private ListIterator<T> iterator;
    private boolean bNext = true;
    private boolean bPrevious = false;
    private T current = null;

    public void add(T node) { list.add(node); }

    public void remove(T node) { list.remove(node); }


    // TODO THIS ONE AS WELL, BUT THAT'S FOR TOMORROW
    public boolean next(boolean bRepeat) {
        if (list.isEmpty()) return false;

        if (iterator == null) {
            iterator = list.listIterator();
        }

        if (!iterator.hasNext()) {
            if (bRepeat) {
                iterator = list.listIterator();
                if (iterator.hasNext()) {
                    current = iterator.next();

                } else return false;

            } else return false;

        } else if (iterator.hasNext()) {
            current = iterator.next();
            if (bPrevious) {

                if (iterator.hasNext()) {
                    current = iterator.next();

                } else if (bRepeat) {
                    iterator = list.listIterator();
                    if (iterator.hasNext()) current = iterator.next();
                    else return false;

                } else return false;
            }
        }

        bNext = true;
        bPrevious = false;
        return true;
    }

    // TODO THIS BAD BOY NEEDS SOME ADDITIONAL WORK
    public boolean previous(boolean bRepeat) {
        if (list.isEmpty()) { return false; }

        if (iterator == null) {
            iterator = list.listIterator(list.size());
        }

        if (!iterator.hasPrevious()) {
            if (bRepeat) {
                iterator = list.listIterator(list.size());
                if (iterator.hasPrevious()) {
                    current = iterator.previous();

                } else return false;

            } else return false;

        } else if (iterator.hasPrevious()) {
            current = iterator.previous();
            if (bNext) {

                if (iterator.hasPrevious()) {
                    current = iterator.previous();

                } else if (bRepeat) {
                    iterator = list.listIterator(list.size());
                    if (iterator.hasPrevious()) current = iterator.previous();
                    else return false;

                } else return false;
            }
        }

        bNext = false;
        bPrevious = true;
        return true;
    }

    public T get() {

        if (current == null) {
            if (!list.isEmpty()) {
                iterator = list.listIterator();
                if (iterator.hasNext()) {
                    current = iterator.next();
                    return current;
                }
            }
        }

        return current;
    }

    public T getFirst() {
        iterator = list.listIterator();
        if (iterator.hasNext()) {
            current = iterator.next();
            bNext = true; bPrevious = false;
            System.out.println("Dropped flags");
            return current;
        }

        return null;
    }

    public T getLast() {
        iterator = list.listIterator(list.size());
        if (iterator.hasPrevious()) {
            current = iterator.previous();
            bNext = false; bPrevious = true;
            System.out.println("Dropped flags");
            return current;
        }

        return null;
    }



    public ArrayList<T> toList() {
        ArrayList<T> arraylist = new ArrayList<>();

        if (list.isEmpty()) return arraylist;

        ListIterator<T> tempIterator = list.listIterator();
        while (tempIterator.hasNext()) {
            arraylist.add(tempIterator.next());
        }

        return arraylist;
    }

    public boolean moveTo(T node) {
        iterator = list.listIterator();
        while (iterator.hasNext()) {
            T element = iterator.next();
            if (element == node) {
                current = element;
                return true;
            }
        }
        return false;
    }



    public void rebuildFrom(ArrayList<T> arrayList) {
        list.clear();
        list.addAll(arrayList);

        if (current == null || !moveTo(current)) {
            if (list.isEmpty()) {
                iterator = null;
                current = null;
            } else {
                iterator = list.listIterator();
                if (iterator.hasNext()) {
                    current = iterator.next();
                } else {
                    iterator = null;
                    current = null;
                }
            }
        }
    }



    //
    // QuickSort
    //
    public void quickSort(Comparator<? super T> comparator) {
        quickSortLinkedList(0, list.size() - 1, comparator);

        iterator = list.listIterator();

        System.out.flush();

        while (iterator.hasNext()) {
            T item = iterator.next();
            if (item instanceof containers.Song) {
                System.out.println(((containers.Song) item).getId() + " " + ((containers.Song) item).getArtist());
            } else {
                System.out.println(item);
            }
        }

    }

    private void quickSortLinkedList(int begin, int end, Comparator<? super T> comparator) {
        if (begin < end) {
            int partitionIndex = partition(begin, end, comparator);

            quickSortLinkedList(begin, partitionIndex-1, comparator);
            quickSortLinkedList(partitionIndex+1, end, comparator);
        }
    }

    private int partition(int begin, int end, Comparator<? super T> comparator) {
        T pivot = list.get(end);
        int i = (begin-1);

        for (int j = begin; j < end; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;

                T temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }

        T temp = list.get(i+1);
        list.set(i+1, list.get(end));
        list.set(end, temp);

        return i+1;
    }
}
