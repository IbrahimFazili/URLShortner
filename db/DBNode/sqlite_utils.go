package main

import (
	"container/list"
	"database/sql"
	"errors"
	"log"
	"os"
	"sync"
	"time"

	_ "github.com/mattn/go-sqlite3"
)

type SqliteJobType uint8

const (
	SQLITE_WRITE  SqliteJobType = iota
	SQLITE_DELETE SqliteJobType = iota
)

type SqliteJob struct {
	entry     DBEntry // entry that this job operates on, can be partially invalid depending on the job type
	jobType   SqliteJobType
	createdAt int64 // unix timestamp (in sec) when the job was queued
}

type SqliteJobExecutor struct {
	conn               *sql.DB
	jobQueue           *list.List
	jobQueueLock       sync.Mutex
	newJobNotification chan bool
	waitTime           int64 // last known wait time for jobs in seconds (time diff between being queued and executed)
}

const DBFilePath = "/virtual/guptalak"
const DBFileName = "URLMappings.db"
const SQLITE_FILE_PREFIX = "file:"
const SQLITE_PRAGMA_ARGS = "?_journal_mode=WAL&_synchronous=NORMAL"

var g_localDB *sql.DB = nil
var g_sqlJobExecutor SqliteJobExecutor

func (executor *SqliteJobExecutor) QueueJob(job SqliteJob) {
	executor.jobQueueLock.Lock()
	defer executor.jobQueueLock.Unlock()

	executor.jobQueue.PushBack(job)
	select {
	case executor.newJobNotification <- true:
		return
	default:
		return
	}
}

func (executor *SqliteJobExecutor) Run() {
	for {
		if executor.jobQueue.Len() > 0 {
			executor.jobQueueLock.Lock()

			nextItem := executor.jobQueue.Front()
			var job SqliteJob = nextItem.Value.(SqliteJob)
			executor.jobQueue.Remove(nextItem)

			executor.jobQueueLock.Unlock()
			executor.waitTime = time.Now().Unix() - job.createdAt

			switch job.jobType {
			case SQLITE_WRITE:
				Sqlite_WriteInternal(job.entry)
				continue
			case SQLITE_DELETE:
				Sqlite_DeleteInternal(job.entry.ShortURL)
				continue
			default:
				continue // should never get here
			}
		} else {
			<-executor.newJobNotification
		}
	}
}

func GetApproxWriteDelay() string {
	return time.Duration(g_sqlJobExecutor.waitTime * int64(time.Second)).String()
}

func AssertNoError(err error, msg string) {
	if err != nil {
		log.Fatalf(msg+": %s\n", err.Error())
	}
}

func Sqlite_InitDBFile() {
	if g_localDB == nil {
		log.Println("Sqlite_InitDBFile: tried to init local db file without active conn to db")
		return
	}

	_, err := g_localDB.Exec("CREATE TABLE `URLMappings` (`short` TEXT PRIMARY KEY, `long` TEXT NOT NULL)")
	AssertNoError(err, "Failed to create URLMappings table")
}

func Sqlite_Connect() bool {
	shouldInitDB := false
	fullPath := DBFilePath + "/" + DBFileName
	if _, err := os.Stat(fullPath); errors.Is(err, os.ErrNotExist) {
		log.Println("SQLite3 db file doesn't exist, creating one")

		os.MkdirAll(DBFilePath, 0700)
		dbfile, err := os.Create(fullPath)
		AssertNoError(err, "Failed to create database file")
		dbfile.Close()

		shouldInitDB = true
		log.Println("SQLite3 db file created")
	} else {
		log.Println("SQLite3 db file already exists, reusing it")
	}

	db, err := sql.Open("sqlite3", SQLITE_FILE_PREFIX+fullPath+SQLITE_PRAGMA_ARGS)
	if err != nil {
		log.Fatalf("Sqlite_Connect: Failed to connect to local sqlite3 database due to error: %s\n", err.Error())
	}

	g_localDB = db

	if shouldInitDB {
		Sqlite_InitDBFile()
	}

	g_sqlJobExecutor = SqliteJobExecutor{conn: g_localDB, jobQueue: list.New().Init(), newJobNotification: make(chan bool)}
	go g_sqlJobExecutor.Run()

	return true
}

func Sqlite_Write(entry DBEntry) bool {
	if g_localDB == nil {
		log.Println("Sqlite_Write: tried to write without active conn to db")
		return false
	}

	if len(entry.ShortURL) == 0 || len(entry.LongURL) == 0 {
		log.Println("Sqlite_Write: tried to write entry with empty long or short url")
		return false
	}

	Sqlite_NewJob(entry, SQLITE_WRITE)

	return true
}

func Sqlite_WriteInternal(entry DBEntry) bool {
	if g_localDB == nil {
		log.Println("Sqlite_Write: tried to write without active conn to db")
		return false
	}

	if len(entry.ShortURL) == 0 || len(entry.LongURL) == 0 {
		log.Println("Sqlite_Write: tried to write entry with empty long or short url")
		return false
	}

	_, err := g_localDB.Exec("REPLACE INTO URLMappings (short, long) VALUES (?, ?);", entry.ShortURL, entry.LongURL)
	if err != nil {
		log.Printf("Sqlite_Write: Failed to insert (%s, %s) to db: %s\n", entry.ShortURL, entry.LongURL, err.Error())
		return false
	}

	return true
}

func Sqlite_Read(shortURL string) *DBEntry {
	if g_localDB == nil {
		log.Println("Sqlite_Read: tried to read without active conn to db")
		return nil
	}

	if len(shortURL) == 0 {
		log.Println("Sqlite_Read: tried to read entry with empty short url")
		return nil
	}

	row := g_localDB.QueryRow("SELECT * FROM URLMappings WHERE short = ?", shortURL)
	entry := DBEntry{}

	err := row.Scan(&entry.ShortURL, &entry.LongURL)
	if err == sql.ErrNoRows {
		log.Printf("Sqlite_Read: no entry found in db with shortURL=%s\n", shortURL)
		return nil
	}

	return &entry
}

func Sqlite_ReadAll() *DBChunk {
	if g_localDB == nil {
		log.Println("Sqlite_ReadAll: tried to read without active conn to db")
		return nil
	}

	rows, err := g_localDB.Query("SELECT * FROM URLMappings")
	if err != nil {
		log.Printf("Sqlite_ReadAll: failed to fetch entries from database")
		return nil
	}

	var data DBChunk
	data.Entries = make([]DBEntry, 0)
	for rows.Next() {
		var short string
		var long string
		err := rows.Scan(&short, &long)
		if err != nil {
			log.Printf("Sqlite_ReadAll: error while building DBChunk: %s\n", err.Error())
			continue
		}

		data.Entries = append(data.Entries, DBEntry{ShortURL: short, LongURL: long})
	}

	return &data
}

func Sqlite_Delete(shortURL string) bool {
	if g_localDB == nil {
		log.Println("Sqlite_Delete: tried to delete without active conn to db")
		return false
	}

	if len(shortURL) == 0 {
		log.Println("Sqlite_Delete: tried to delete entry with empty short url")
		return false
	}

	Sqlite_NewJob(DBEntry{ShortURL: shortURL, LongURL: ""}, SQLITE_DELETE)

	return true
}

func Sqlite_DeleteInternal(shortURL string) bool {
	if g_localDB == nil {
		log.Println("Sqlite_Delete: tried to delete without active conn to db")
		return false
	}

	if len(shortURL) == 0 {
		log.Println("Sqlite_Delete: tried to delete entry with empty short url")
		return false
	}

	_, err := g_localDB.Exec("DELETE FROM URLMappings WHERE short = ?", shortURL)
	if err != nil {
		log.Printf("Sqlite_Delete: error deleting %s from db - %s\n", shortURL, err.Error())
		return false
	}

	return true
}

func Sqlite_NewJob(entry DBEntry, jobType SqliteJobType) {
	g_sqlJobExecutor.QueueJob(SqliteJob{entry: entry, jobType: jobType, createdAt: time.Now().Unix()})
}
