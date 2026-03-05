import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
  Gender, AgeGroup, Region, Ethnicity, ReligiousView, PoliticalView, RelationshipStatus,
  GENDER_LABELS, AGE_GROUP_LABELS, REGION_LABELS, ETHNICITY_LABELS,
  RELIGIOUS_VIEW_LABELS, POLITICAL_VIEW_LABELS, RELATIONSHIP_STATUS_LABELS,
} from '../types/enums';
import Button from '../components/common/Button';
import ErrorBanner from '../components/common/ErrorBanner';
import styles from './RegisterPage.module.css';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [gender, setGender] = useState<Gender>(Gender.PREFER_NOT_TO_SAY);
  const [ageGroup, setAgeGroup] = useState<AgeGroup>(AgeGroup.AGE_18_24);
  const [region, setRegion] = useState<Region>(Region.NORTHEAST);
  const [ethnicity, setEthnicity] = useState<Ethnicity>(Ethnicity.PREFER_NOT_TO_SAY);
  const [religiousView, setReligiousView] = useState<ReligiousView | ''>('');
  const [politicalView, setPoliticalView] = useState<PoliticalView | ''>('');
  const [relationshipStatus, setRelationshipStatus] = useState<RelationshipStatus | ''>('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register({
        displayName, email, password, gender, ageGroup, region, ethnicity,
        ...(religiousView ? { religiousView } : {}),
        ...(politicalView ? { politicalView } : {}),
        ...(relationshipStatus ? { relationshipStatus } : {}),
      });
      navigate('/');
    } catch {
      setError('Registration failed. Email may already be taken.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.wrapper}>
      <form className={styles.form} onSubmit={handleSubmit}>
        <h1 className={styles.heading}>
          Join <span className={styles.brand}>Hot Take</span> Ranker
        </h1>
        <ErrorBanner message={error} />

        <input
          placeholder="Display Name"
          value={displayName}
          onChange={(e) => setDisplayName(e.target.value)}
          required
        />
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          minLength={6}
        />

        <div className={styles.row}>
          <div className={styles.field}>
            <label>Gender</label>
            <select value={gender} onChange={(e) => setGender(e.target.value as Gender)}>
              {Object.entries(GENDER_LABELS).map(([val, label]) => (
                <option key={val} value={val}>{label}</option>
              ))}
            </select>
          </div>
          <div className={styles.field}>
            <label>Age Group</label>
            <select value={ageGroup} onChange={(e) => setAgeGroup(e.target.value as AgeGroup)}>
              {Object.entries(AGE_GROUP_LABELS).map(([val, label]) => (
                <option key={val} value={val}>{label}</option>
              ))}
            </select>
          </div>
        </div>

        <div className={styles.row}>
          <div className={styles.field}>
            <label>Region</label>
            <select value={region} onChange={(e) => setRegion(e.target.value as Region)}>
              {Object.entries(REGION_LABELS).map(([val, label]) => (
                <option key={val} value={val}>{label}</option>
              ))}
            </select>
          </div>
          <div className={styles.field}>
            <label>Ethnicity</label>
            <select value={ethnicity} onChange={(e) => setEthnicity(e.target.value as Ethnicity)}>
              {Object.entries(ETHNICITY_LABELS).map(([val, label]) => (
                <option key={val} value={val}>{label}</option>
              ))}
            </select>
          </div>
        </div>

        <div className={styles.row}>
          <div className={styles.field}>
            <label>Religion (optional)</label>
            <select value={religiousView} onChange={(e) => setReligiousView(e.target.value as ReligiousView | '')}>
              <option value="">— Select —</option>
              {Object.entries(RELIGIOUS_VIEW_LABELS).map(([val, label]) => (
                <option key={val} value={val}>{label}</option>
              ))}
            </select>
          </div>
          <div className={styles.field}>
            <label>Politics (optional)</label>
            <select value={politicalView} onChange={(e) => setPoliticalView(e.target.value as PoliticalView | '')}>
              <option value="">— Select —</option>
              {Object.entries(POLITICAL_VIEW_LABELS).map(([val, label]) => (
                <option key={val} value={val}>{label}</option>
              ))}
            </select>
          </div>
        </div>

        <div className={styles.row}>
          <div className={styles.field}>
            <label>Relationship (optional)</label>
            <select value={relationshipStatus} onChange={(e) => setRelationshipStatus(e.target.value as RelationshipStatus | '')}>
              <option value="">— Select —</option>
              {Object.entries(RELATIONSHIP_STATUS_LABELS).map(([val, label]) => (
                <option key={val} value={val}>{label}</option>
              ))}
            </select>
          </div>
          <div className={styles.field} />
        </div>

        <Button type="submit" full disabled={loading}>
          {loading ? 'Creating account...' : 'Sign Up'}
        </Button>

        <div className={styles.link}>
          Already have an account? <Link to="/login">Log in</Link>
        </div>
      </form>
    </div>
  );
}
