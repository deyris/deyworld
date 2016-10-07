package es.barcelona.dey.memoke.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import es.barcelona.dey.memoke.R;
import es.barcelona.dey.memoke.beans.Pair;
import es.barcelona.dey.memoke.presenters.CreationPresenter;
import es.barcelona.dey.memoke.views.CreationView;

/**
 * Created by deyris.drake on 24/1/16.
 */
public class CreationActivity extends AppCompatActivity implements CreationView, ContentFragment.OnDataPass, ContentFragment.FragmentIterationListener{

    private CreationFragment mCreationFragment;
    private   ContentFragment mContentFragment;
//    public static Board mBoard;

    CreationPresenter creationPresenter;

    Bundle contentBundle;

    @Override
    public Context getContext(){
        return this;
    }

    @Override
    public Bundle actualizeBundle(Bundle bundle, String nameData, String data) {

        if (null == bundle) {
            bundle = new Bundle();
        }
        bundle.putString(nameData, data);
        return bundle;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("DEY", "Estoy en CreationActivity.onDestroy");
    }

    public void onFragmentIteration(Bundle arguments){
        if (mContentFragment!=null && arguments!=null && arguments.get(CreationPresenter.PARAM_CURRENT_PAIR)!=null){
            contentBundle = arguments;
        }

        if (arguments!=null && arguments.get("TEXT")!=null){
            contentBundle = arguments;
        }
    }

      @Override
    public void onSaveInstanceState(Bundle outState) {
          super.onSaveInstanceState(outState);

          if (null != creationPresenter.getmBoard())   {
              //Salvamos lo que hay en mBoard
              creationPresenter.updateOrAddBoard(creationPresenter.getmBoard());
              //Guardamos el id VISUALIZADO en el momento de irnos
              outState.putInt(CreationPresenter.PARAM_CURRENT_PAIR_NUMBER,creationPresenter.getIdCurrentPair());
              outState.putString(CreationPresenter.PARAM_CURRENT_BOARD, creationPresenter.getJsonCurrentBoard(creationPresenter.getmBoard()));
          }

      }
    private boolean fragmentAlreadyRestoredFromSavedState(String tag) {
        return (getFragmentManager().findFragmentByTag(tag) != null ? true : false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_creation);

            creationPresenter = new CreationPresenter();
            creationPresenter.setView(this);

            // Get a reference to the FragmentManager
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            FragmentManager fragmentManager = getFragmentManager();

            //Verificamos si venimos o no de un fichero ya existente
           // Bundle bundleFromMain = getIntent().getExtras();
           // String title = "";

            creationPresenter.updateIdCurrentPairIfExistInContext(savedInstanceState);
            creationPresenter.updateBoardIfExistIncontent(savedInstanceState);
            savedInstanceState = creationPresenter.prepareForContentFragmentFirstLoad(getIntent().getExtras(),savedInstanceState);
           /* boolean existeContentFragment = fragmentAlreadyRestoredFromSavedState(ContentFragment.TAG);
            if(!existeContentFragment) { //Primera vez que se carga el fragment
                Pair currentPair;
                currentPair = creationPresenter.generateNextPair(bundleFromMain);

                //Actualizamos bundle
                savedInstanceState = this.actualizeBundle(savedInstanceState,PARAM_CURRENT_PAIR,creationPresenter.getJsonCurrentPair(currentPair));

                //Actualizamos mBoard
                creationPresenter.getBoardWithTitleFromMain(bundleFromMain);

            }*/
            //Si giro el móvil, vengo a esta línea, no es primera vez que se carga el fragment
            Bundle bundle = new Bundle();
            bundle.putInt("CURRENT_PAIR", creationPresenter.getIdCurrentPair());

            mCreationFragment = new CreationFragment();
            mCreationFragment.setArguments(bundle);

            fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.header_frame, mCreationFragment, CreationFragment.TAG);
            fragmentTransaction.commit();

            if (!creationPresenter.fragmentAlreadyRestoredFromSavedState(ContentFragment.TAG)) {
                fragmentManager.beginTransaction().add(R.id.content_frame,
                        ContentFragment.newInstance(savedInstanceState),
                        ContentFragment.TAG).addToBackStack(ContentFragment.TAG).commit();
            }

            //Inicializamos botones

         //Boton siguiente
        Button btnSgte = (Button) findViewById(R.id.btnSgte);
        btnSgte.setVisibility(View.GONE);
        setListenerBtnSgte();

        //Boton anterior
        Button btnAnt = (Button)findViewById(R.id.btnAnt);
        btnAnt.setVisibility(View.GONE);
        setListenerBtnAnterior();

        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.creation_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.showBoardTab:
                Intent i = new Intent(this, BoardActivity.class);
                i.putExtra(CreationPresenter.PARAM_CURRENT_BOARD,creationPresenter.getJsonCurrentBoard(creationPresenter.getmBoard()));
                startActivity(i);
                return true;
            default:
                return false;
        }
    }


    @Override
    public void onDataPass(int data) {
        ContentFragment f = (ContentFragment)getFragmentManager().findFragmentByTag(ContentFragment.TAG);
        f.receivingFromDialog(data);

    }

    @Override
    public void onDataPass(EditText data) {
        ContentFragment f = (ContentFragment)getFragmentManager().findFragmentByTag(ContentFragment.TAG);
        f .receivingFromDialog(data);

    }

    public void setListenerBtnSgte(){
        Button btnSgte = (Button)findViewById(R.id.btnSgte);
        btnSgte.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                ContentFragment f = (ContentFragment) getFragmentManager().findFragmentByTag(ContentFragment.TAG);
                Pair pair = f.getmCurrentPair();

                Bundle bundleSgte = new Bundle();
                if (creationPresenter.pairNotSavedYet(pair)) {

                    pair.setNumber(creationPresenter.getIdCurrentPair());

                    creationPresenter.inicializeBoardIfPairsAreNull();


                    //Verificamos si ya pair existe para agregarlo o modificarlo
                    creationPresenter.savePairInBoard(pair);

                    //Vaciamos fragment y nos vamos al sgte
                    putFragmentEmptyAndGoNext(bundleSgte);

                    //Ponemos el boton Siguiente invisible de nuevo
                    Button button = (Button) findViewById(v.getId());
                    button.setVisibility(View.GONE);

                } else {

                    putFragmentEmptyAndGoNext(bundleSgte);

                    //Rescatamos la pareja
                    String jsonNextPair = creationPresenter.getNextPairOnBoard(creationPresenter.getIdCurrentPair());

                    //Rellenamos Bundle con la pareja siguiente
                    bundleSgte.putSerializable(CreationPresenter.PARAM_CURRENT_PAIR, jsonNextPair);

                }
                //Actualizamos creationFragment con el numero de la pareja
                actualicePairNumber();


            }
        });
    }

    public void putFragmentEmptyAndGoNext(Bundle bundleSgte){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        //Incrementamos la pareja y pasamos el bundle
        creationPresenter.incrementIdCurrentPair();
        bundleSgte.putInt(CreationPresenter.PARAM_CURRENT_PAIR_NUMBER, creationPresenter.getIdCurrentPair());

        ft.setCustomAnimations(R.animator.slide_in_up, R.animator.slide_out_up).replace(R.id.content_frame,
                ContentFragment.newInstance(bundleSgte),
                ContentFragment.TAG).addToBackStack(null).commit();


    }

    public void putFragmentOnPast(){
        creationPresenter.decrementIdCurrentPair();
        Pair pairAnt = creationPresenter.getmBoard().getPairs().get(creationPresenter.getIdCurrentPair());

        //Actualizamos fragment
        Bundle bundleAnt = new Bundle();
        String jsonPairAnt = creationPresenter.getJsonCurrentPair(pairAnt);

        bundleAnt.putSerializable(CreationPresenter.PARAM_CURRENT_PAIR,jsonPairAnt);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft =fragmentManager.beginTransaction();

        ft.setCustomAnimations(R.animator.slide_out_up_ant, R.animator.slide_in_up_ant).replace(R.id.content_frame,
                ContentFragment.newInstance(bundleAnt),
                ContentFragment.TAG).addToBackStack(null).commit();
    }

    public void actualicePairNumber(){
        //Actualizamos creationFragment con el numero de la pareja
        CreationFragment cf = (CreationFragment) getFragmentManager().findFragmentByTag(CreationFragment.TAG);
        Bundle bundleFromMain = getIntent().getExtras();

        if (null != cf) {
            cf.mTxtNumber.setText(String.format(getResources().getString(R.string.creation_number), creationPresenter.getIdCurrentPair()));

        }
    }

    public void setListenerBtnAnterior(){
        Button btnAnt = (Button)findViewById(R.id.btnAnt);

        btnAnt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //En caso de que se haya completado el estado de la pareja, guardamos
                ContentFragment f = (ContentFragment)getFragmentManager().findFragmentByTag(ContentFragment.TAG);
                Pair pairForSave = f.getmCurrentPair();
                if (pairForSave.getState().equals(Pair.State.COMPLETED)) {
                    creationPresenter.savePairInBoard(pairForSave);
                }

                putFragmentOnPast();

                setListenerBtnSgte();

                //Actualizamos creationFragment con el numero de la pareja
                CreationFragment cf = (CreationFragment) getFragmentManager().findFragmentByTag(CreationFragment.TAG);

                if (null != cf) {
                    cf.mTxtNumber.setText(String.format(getResources().getString(R.string.creation_number), creationPresenter.getIdCurrentPair()));

                }

            }
        });
    }
}
