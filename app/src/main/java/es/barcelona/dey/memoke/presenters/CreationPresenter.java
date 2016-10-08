package es.barcelona.dey.memoke.presenters;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import java.util.HashMap;

import es.barcelona.dey.memoke.R;
import es.barcelona.dey.memoke.beans.Board;
import es.barcelona.dey.memoke.beans.Pair;
import es.barcelona.dey.memoke.database.BoardDatabase;
import es.barcelona.dey.memoke.interactors.CreationInteractor;
import es.barcelona.dey.memoke.ui.ContentFragment;
import es.barcelona.dey.memoke.ui.CreationFragment;
import es.barcelona.dey.memoke.ui.MainFragment;
import es.barcelona.dey.memoke.views.CreationView;

/**
 * Created by deyris.drake on 18/9/16.
 */
public class CreationPresenter extends ComunPresenter implements Presenter<CreationView>{

    public static String PARAM_CURRENT_PAIR = "PARAM_CURRENT_PAIR";
    public static String PARAM_CURRENT_PAIR_NUMBER = "PARAM_CURRENT_PAIR_NUMBER";
    public static String PARAM_CURRENT_BOARD ="PARAM_CURRENT_BOARD";

    CreationView creationView;
    CreationInteractor creationInteractor;

    int idCurrentPair;
    Board mBoard;

    @Override
    public void setView(CreationView view) {
        if (view == null) throw new IllegalArgumentException("You can't set a null view");

        creationView = view;
        creationInteractor = new CreationInteractor(creationView.getContext());
    }

    @Override
    public void detachView() {
        creationView = null;
    }

    public void updateOrAddBoard(Board board){
        BoardDatabase.updateOrAddBoard(creationView.getContext(),board);
    }

    public void savePairInBoard(Pair pair){
        creationInteractor.savePairInBoard(this.mBoard, pair);
    }

    public int getIdCurrentPair() {

        return idCurrentPair;
    }

    public void setIdCurrentPair(int idCurrentPair) {
        this.idCurrentPair = idCurrentPair;
    }

    private Pair loadPairFromExistingBoard(String jsonBoard){
        Board mBoard = gson.fromJson(jsonBoard, Board.class);
        //Buscamos currentPair
        Pair currentPair = new Pair();
        if (null!=mBoard.getPairs()) {
            this.idCurrentPair = (null != mBoard.getPairs()) ? mBoard.getPairs().size() : 1;
            //Actualizamos currentPair
            currentPair = mBoard.getPairs().get(this.idCurrentPair);
            this.mBoard = mBoard;
        }else{
            //Es un tablero vacío, que existe pero no tiene ninguna pareja aún
            this.idCurrentPair = 1;

        }

        return currentPair;
    }



    private Pair generateNewPair(){
        //Creamos tablero
        mBoard = new Board();
        idCurrentPair = 1;
        Pair newPair = new Pair();

        return newPair;
    }

    public void incrementIdCurrentPair(){
        this.idCurrentPair++;
    }

    public void decrementIdCurrentPair(){
        this.idCurrentPair--;
    }

    public Pair generateNextPair(Bundle bundleFromMain){
        Pair currentPair;
        if (bundleFromMain.getString(MainFragment.PARAM_SELECTED_BOARD) != null) {  //Hay un board que ya existe que viene del view anterior

            currentPair =  loadPairFromExistingBoard(bundleFromMain.getString(MainFragment.PARAM_SELECTED_BOARD));
        } else {
            //Creamos tablero
            currentPair = generateNewPair();

        }
        return currentPair;
    }

    public boolean fragmentAlreadyRestoredFromSavedState(String tag) {
        return (creationView.getFragmentManager().findFragmentByTag(tag) != null ? true : false);
    }

    public Bundle prepareForContentFragmentFirstLoad(Bundle bundleFromMainActivity, Bundle savedInstanceState){
        boolean existeContentFragment = fragmentAlreadyRestoredFromSavedState(ContentFragment.TAG);
        if(!existeContentFragment) { //Primera vez que se carga el fragment
            Pair currentPair;
            currentPair = generateNextPair(bundleFromMainActivity);

            //Actualizamos bundle
            savedInstanceState = creationView.actualizeBundle(savedInstanceState,PARAM_CURRENT_PAIR,getJsonCurrentPair(currentPair));

            //Actualizamos mBoard
            getBoardWithTitleFromMain(bundleFromMainActivity);

        }

        //Si giro el móvil, vengo a esta línea, no es primera vez que se carga el fragment
       /* Bundle bundle = new Bundle();
        bundle.putInt("CURRENT_PAIR", getIdCurrentPair());

        CreationFragment = mCreationFragment = new CreationFragment();
        mCreationFragment.setArguments(bundle);

        fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.header_frame, mCreationFragment, CreationFragment.TAG);
        fragmentTransaction.commit();*/




        return savedInstanceState;
    }

    public void createCreationActivity(Bundle savedInstanceState, Bundle bundleFromMain, FragmentManager fragmentManager, FragmentTransaction fragmentTransaction){
        //Verificamos si venimos o no de un fichero ya existente
        // Bundle bundleFromMain = getIntent().getExtras();
        // String title = "";

        updateIdCurrentPairIfExistInContext(savedInstanceState);
        updateBoardIfExistInContent(savedInstanceState);
        savedInstanceState = prepareForContentFragmentFirstLoad(bundleFromMain,savedInstanceState);

        prepareForContentFragmentForRotate(savedInstanceState, fragmentManager,fragmentTransaction);


        //Inicializamos botones

        //Boton siguiente
        creationView.inicializeButtonNext();

        //Boton anterior
        creationView.inicializeButtonPast();
    }
    public void prepareForContentFragmentForRotate(Bundle savedInstanceState, FragmentManager fragmentManager, FragmentTransaction fragmentTransaction){
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM_CURRENT_PAIR, getIdCurrentPair());

        CreationFragment mCreationFragment = new CreationFragment();
        mCreationFragment.setArguments(bundle);

        fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.header_frame, mCreationFragment, CreationFragment.TAG);
        fragmentTransaction.commit();

        if (!fragmentAlreadyRestoredFromSavedState(ContentFragment.TAG)) {
            fragmentManager.beginTransaction().add(R.id.content_frame,
                    ContentFragment.newInstance(savedInstanceState),
                    ContentFragment.TAG).addToBackStack(ContentFragment.TAG).commit();
        }

    }

    public void updateIdCurrentPairIfExistInContext(Bundle savedInstanceState){
        if(null!= savedInstanceState){
            this.setIdCurrentPair(savedInstanceState.getInt(PARAM_CURRENT_PAIR_NUMBER));
        }
    }
    public void updateBoardIfExistInContent(Bundle savedInstanceState){
        if(null!= savedInstanceState){
            this.setmBoard(this.getCurrentBoard(savedInstanceState.getString(PARAM_CURRENT_BOARD)));
        }
    }

    private void updateTitleFromBundle(Bundle bundleFromMain){
        //Actualizamos title tablero
        if (null==mBoard){
            mBoard = new Board();
        }
        if (bundleFromMain.getString(MainFragment.PARAM_TITLE) != null) {
            String title = bundleFromMain.getString(MainFragment.PARAM_TITLE).toString();
            mBoard.setTitle(title);
        }

    }
    public Board getBoardWithTitleFromMain(Bundle bundleFromMain){
        updateTitleFromBundle(bundleFromMain);
        return mBoard;
    }
    public Board getBoardWithTitleFromMain() {
        return mBoard;
    }

    public void setmBoard(Board mBoard) {
        this.mBoard = mBoard;
    }


    public boolean pairNotSavedYet(Pair pair){
        return null!=pair && (pair.getState().equals(Pair.State.COMPLETED) || pair.getState().equals(Pair.State.IN_PROCESS));
    }

    public String getNextPairOnBoard(int mCurrentPair){
        Pair pairSgte = new Pair();
        String jsonPairAnt = null;
        if (mBoard.getPairs().size() >= mCurrentPair) {
            pairSgte = mBoard.getPairs().get(mCurrentPair);
            jsonPairAnt = gson.toJson(pairSgte);
        }

        return jsonPairAnt;

    }

    public void inicializeBoardIfPairsAreNull(){
        if (null == mBoard.getPairs()) {
            mBoard.setPairs(new HashMap<Integer, Pair>());
        }
    }

    public Board getmBoard() {
        return mBoard;
    }
}
