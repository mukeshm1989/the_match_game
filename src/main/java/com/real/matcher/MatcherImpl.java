package com.real.matcher;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.real.dto.ActorDetail;
import com.real.dto.ExternalMovieDetails;
import com.real.dto.InternalMovieDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.real.dto.ResponseEntity.*;

public class MatcherImpl implements Matcher {


  private static final Logger LOGGER = LoggerFactory.getLogger(MatcherImpl.class);
  List<InternalMovieDetail> internalMovieDB= new ArrayList<>();
  public MatcherImpl(CsvStream movieDb, CsvStream actorAndDirectorDb) {
    LOGGER.info("importing database");
    // TODO implement me
    // Assuming files are valid, so skipping the step to validate file.

    List<InternalMovieDetail> movieDetailsList = readMovieDB(movieDb);
    List<ActorDetail> actorDetailList= readActorAndDirectorDb(actorAndDirectorDb);
    readActorsAndDirectorsDetailsAndMapToMovieId(actorDetailList, movieDetailsList);
    LOGGER.info("database imported");
  }

  private void readActorsAndDirectorsDetailsAndMapToMovieId(List<ActorDetail> actorDetailList, List<InternalMovieDetail> movieDetailsList) {
    LOGGER.info("reading data for actors and director details and mapped to the unique movieId");
    Map<String,List<ActorDetail>> map = actorDetailList.parallelStream().collect(Collectors.groupingBy(ActorDetail::getMovieId,Collectors.toList()));
    for (InternalMovieDetail movieDetail : movieDetailsList) {
      List<String> actors = new ArrayList<>();
      if(map.containsKey(movieDetail.getMovieId())) {
        List<ActorDetail> actorDetails= map.get(movieDetail.getMovieId());
        actorDetails.forEach(actorDetail->{
          if(actorDetail.getRole().equals(DIRECTOR_INTERNAL)) {
            movieDetail.setDirector(actorDetail.getName());
          }else {
            actors.add(actorDetail.getName());
          }
        });
        movieDetail.setActors(actors);
      }
      internalMovieDB.add(movieDetail);
    }
    LOGGER.info("reading amd mapping data to movie id has been completed for actors and director details");
  }

  private List<ActorDetail> readActorAndDirectorDb(CsvStream actorAndDirectorDb) {
    LOGGER.info("reading data for actors and director details ");
    Map<String, Integer> actorDirectorMapIndex = getMapIndex(actorAndDirectorDb.getHeaderRow());
    Stream<String> actorAndDirecotorDetailsStream = actorAndDirectorDb.getDataRows();
    List<ActorDetail> actorDetailList = new ArrayList<>();
    actorAndDirecotorDetailsStream.forEach(actor->{
      String [] movieArr=actor.split(REGEX);
      ActorDetail actorDetail = new ActorDetail();
      actorDetail.setMovieId(movieArr[actorDirectorMapIndex.get(MOVIE_ID)]);
      actorDetail.setName(movieArr[actorDirectorMapIndex.get(NAME)]);
      actorDetail.setRole(movieArr[actorDirectorMapIndex.get(ROLE)]);
      actorDetailList.add(actorDetail);
    });
    LOGGER.info("reading data for actors and director details has been completed");
    return actorDetailList;
  }

  private  List<InternalMovieDetail> readMovieDB(CsvStream movieDb) {
    LOGGER.info("reading movie data");
    List<InternalMovieDetail> movieDetailsList = new ArrayList<>();
    Map<String, Integer> movieMapIndex = getMapIndex(movieDb.getHeaderRow());
    Stream<String> moviedetailsStream =movieDb.getDataRows();
    moviedetailsStream.forEach(x->{
      String [] movieArr=x.split(REGEX);
      InternalMovieDetail internalMovieDetail = new InternalMovieDetail();
      internalMovieDetail.setMovieId(movieArr[movieMapIndex.get(ID)]);
      internalMovieDetail.setTitle(movieArr[movieMapIndex.get(TITLE)]);
      movieDetailsList.add(internalMovieDetail);
    });

    LOGGER.info("reading movie data is completed ");
    return  movieDetailsList;
  }

  @Override
  public List<IdMapping> match(DatabaseType databaseType, CsvStream externalDb) {
    // TODO implement me
    LOGGER.info("Filtering for external data internal data");
    Map<String, Integer> externalMapIndex = getMapIndex(externalDb.getHeaderRow());
    Stream<String> externalDbStream = externalDb.getDataRows();
    List<ExternalMovieDetails> externalMovieDetailsList = new ArrayList<>();
    externalDbStream.forEach(actor->{
      String [] movieArr=actor.split(REGEX_NESTED_COMMA, -1);
      boolean isNotNull = validateNullValue(movieArr[externalMapIndex.get(MEDIA_ID)],
              movieArr[externalMapIndex.get(EXTERNAL_TITLE)],movieArr[externalMapIndex.get(MEDIA_TYPE)],
              movieArr[externalMapIndex.get(ACTORS)],movieArr[externalMapIndex.get(DIRECTOR)],
              movieArr[externalMapIndex.get(XBOX_LIVE_URL)]);
      readExternalDB(isNotNull, movieArr, externalMapIndex, externalMovieDetailsList);
    });
    LOGGER.info("Filtering for external data internal data is completed");
    return getMatchIdFromInternalAndExternalData(internalMovieDB,externalMovieDetailsList);
  }

  private static void readExternalDB(boolean isNotNull, String[] movieArr, Map<String, Integer> externalMapIndex, List<ExternalMovieDetails> externalMovieDetailsList) {
    LOGGER.info("Reading external data");
    if(isNotNull) {
      ExternalMovieDetails externalMovieDetail = new ExternalMovieDetails();
      externalMovieDetail.setMediaId(movieArr[externalMapIndex.get(MEDIA_ID)]);
      externalMovieDetail.setTitle(movieArr[externalMapIndex.get(EXTERNAL_TITLE)]);
      externalMovieDetail.setMediaType(movieArr[externalMapIndex.get(MEDIA_TYPE)]);
      String actorString= movieArr[externalMapIndex.get(ACTORS)].replace(REGEX_TO_REMOVE_DOUBLE_QUOTE , REPLACE_WITH_EMPTY);
      externalMovieDetail.setActors(Arrays.asList(actorString.split(REGEX)));
      externalMovieDetail.setDirector(movieArr[externalMapIndex.get(DIRECTOR)]);
      externalMovieDetail.setXboxLiveURL(movieArr[externalMapIndex.get(XBOX_LIVE_URL)]);
      externalMovieDetailsList.add(externalMovieDetail);
    }
    LOGGER.info("Reading external data is completed ");
  }

  public boolean validateNullValue(String mediaId,String title,String mediaType,String actors,String director,String id) {
    LOGGER.info("Validating null value ");
    boolean flag = false;
    if((mediaId!=null &&!mediaId.isEmpty()) && (title!=null &&!title.isEmpty()) && (mediaType!=null &&!mediaType.isEmpty()) &&
            (actors!=null &&!actors.isEmpty()) && (director!=null &&!director.isEmpty()) && (id!=null &&!id.isEmpty())){
      flag = true;
    }
    LOGGER.info("Validating null value is completed");
    return flag;
  }


  private List<IdMapping> getMatchIdFromInternalAndExternalData(List<InternalMovieDetail> internalMovieDB, List<ExternalMovieDetails> externalMovieDetailsList) {
    LOGGER.info("Get all matching record from internal and external data");
    Set<IdMapping> idMappingList = new HashSet<>();
    filterMatchingDataBasedOnTitleDirectorsAndActors(internalMovieDB, externalMovieDetailsList, idMappingList);
    LOGGER.info("Get all matching record from internal and external data is completed ");
    return new ArrayList<>(idMappingList);
  }

  private static void filterMatchingDataBasedOnTitleDirectorsAndActors(List<InternalMovieDetail> internalMovieDB, List<ExternalMovieDetails> externalMovieDetailsList, Set<IdMapping> idMappingList) {
    LOGGER.info("Filtering data based on the actors, directors and title");
    internalMovieDB.parallelStream().map(internalData -> {
      externalMovieDetailsList.parallelStream().anyMatch(externalData -> {
        if (null != internalData.getDirector() && null != internalData.getTitle() &&
                internalData.getTitle().equalsIgnoreCase(externalData.getTitle()) &&
                internalData.getDirector().equalsIgnoreCase(externalData.getDirector())
                && internalData.getActors().retainAll(externalData.getActors())) {
          idMappingList.add(new IdMapping(Integer.parseInt(internalData.getMovieId()), externalData.getMediaId()));
        }
        return false;
      });
      return idMappingList;
    }).collect(Collectors.toList());
    LOGGER.info("Filtering data based on the actors, directors and title is completed ");
  }

  private Map<String,Integer> getMapIndex(String headerRow){
    Map<String,Integer> headerMap = new HashMap<>();
    String[] headerArr =headerRow.split(REGEX);
    int index=0;
    for(String header : headerArr) {
      headerMap.put(header,index);
      index++;
    }
    return headerMap;
  }
}