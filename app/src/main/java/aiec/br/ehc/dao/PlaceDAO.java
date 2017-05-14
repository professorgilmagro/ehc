package aiec.br.ehc.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import aiec.br.ehc.helper.DateHelper;
import aiec.br.ehc.model.Place;
import aiec.br.ehc.helper.ManifestHelper;

/**
 * Provém a persistência de dados para os locais
 *
 * @author Gilmar Soares <professorgilmagro@gmail.com>
 * @author Ricardo Boreto <ricardoboreto@gmail.com>
 * @since 2017-05-07
 */
public class PlaceDAO extends SQLiteOpenHelper {
    static final private String TABLE_NAME = "places";

    public PlaceDAO(Context context) {
        super(
                context,
                ManifestHelper.from(context).getMetadata("DATABASE").toString(),
                null,
                (int) ManifestHelper.from(context).getMetadata("VERSION")
        );
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = String.format(
                "CREATE TABLE %s(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "host VARCHAR(50)," +
                        "port INTEGER default 80," +
                        "name VARCHAR(100)," +
                        "description TEXT," +
                        "icon TEXT," +
                        "creation_date TEXT," +
                        "modification_date TEXT," +
                        "created_by TEXT," +
                        "modified_by TEXT" +
                        ")",
                TABLE_NAME
        );

        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(sqLiteDatabase);
    }

    /**
     * Permite gravar os dados do local com base na instância do mesmo
     * Este método decide se deve atualizar ou inserir de maneira transparente
     *
     * @param place instancia do local
     */
    public void save(Place place) {
        if (place.getId() != null) {
            this.update(place);
        } else {
            this.insert(place);
        }
    }

    /**
     * Insere um novo local
     *
     * @param place instância do local a ser gravada
     */
    public void insert(Place place) {
        place.setCreatedAt(new Date());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_NAME, null, this.getContentValues(place));
    }

    /**
     * Atualiza o local
     *
     * @param place Instância do local
     */
    public void update(Place place) {
        place.setModificationAt(new Date());
        String[] params = {place.getId().toString()};
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME, this.getContentValues(place), "id = ?", params);
    }

    /**
     * Retorna o conteúdo com base na instância do local
     *
     * @param place instância do local
     * @return ContentValues
     */
    private ContentValues getContentValues(Place place) {
        ContentValues data = new ContentValues();

        if (place.getId() != null) {
            data.put("id", place.getId());
        }

        data.put("name", place.getName());
        data.put("description", place.getDescription());
        data.put("host", place.getHost());
        data.put("port", place.getPort());
        data.put("icon", place.getIcon());
        data.put("creation_date", DateHelper.asIsoDateTime(place.getCreatedAt()));
        data.put("modification_date", DateHelper.asIsoDateTime(place.getModificationAt()));
        data.put("created_by", place.getCreatedBy());
        data.put("modified_by", place.getModifiedBy());
        return data;
    }

    /**
     * Retorna todos os registros dos locais contidos na tabela
     *
     * @return List<Place>
     */
    public List<Place> getAll() {
        String sql = String.format("SELECT * FROM %s", TABLE_NAME);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);
        List<Place> places = new ArrayList<>();
        while (c.moveToNext()) {
            places.add(this.fillByCursor(c));
        }

        c.close();
        return places;
    }

    /**
     * Retorna todos os registros de locals contidos na tabela
     *
     * @return Place
     */
    public Place getById(Integer id) {
        String[] params = {id.toString()};
        String sql = String.format("SELECT * FROM %s WHERE id = ?", TABLE_NAME);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, params);
        Place place = this.fillByCursor(c);
        c.close();
        return place;
    }

    /**
     * Cria uma instância de Place com base nos dados oriundos de um cursor do recordset
     *
     * @param c Cursor
     * @return Place
     */
    private Place fillByCursor(Cursor c) {
        Place place = new Place();
        if (c.getCount() > 0) {
            place.setId(c.getInt(c.getColumnIndex("id")));
            place.setName(c.getString(c.getColumnIndex("name")));
            place.setDescription(c.getString(c.getColumnIndex("description")));
            place.setHost(c.getString(c.getColumnIndex("host")));
            place.setPort(c.getInt(c.getColumnIndex("port")));
            place.setIcon(c.getString(c.getColumnIndex("icon")));
            place.setCreatedBy(c.getString(c.getColumnIndex("created_by")));
            place.setModifiedBy(c.getString(c.getColumnIndex("modified_by")));
            place.setCreatedAt(DateHelper.asDate(c.getString(c.getColumnIndex("creation_date"))));
            place.setModificationAt(DateHelper.asDate(c.getString(c.getColumnIndex("modification_date"))));
        }

        return place;
    }

    /**
     * Permite excluir um local com base na instância do mesmo
     *
     * @param place instância do Local
     */
    public void delete(Place place) {
        SQLiteDatabase db = getWritableDatabase();
        String[] params = {place.getId().toString()};
        db.delete(TABLE_NAME, "id = ?", params);
    }
}
