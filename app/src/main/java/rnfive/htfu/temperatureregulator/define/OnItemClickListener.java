package rnfive.htfu.temperatureregulator.define;

public interface OnItemClickListener {
    void onItemClick(int pos);
    void onItemLongClick(int pos);
    void onItemDeleted();
    void onItemEdit(int pos);
}
