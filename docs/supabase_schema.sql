-- Minimal schema + RLS for PlanWise (recommended)
create table if not exists public.planwise_events (
  id text primary key,
  user_id uuid not null,
  title text not null,
  start_datetime bigint not null,
  end_datetime bigint null,
  category_id text not null,
  subcategory_id text null,
  color integer not null,
  location_text text null,
  description text null,
  recurrence_type text not null,
  recurrence_interval integer not null,
  reminders jsonb not null,
  created_at bigint not null,
  updated_at bigint not null,
  deleted boolean not null default false
);

create table if not exists public.planwise_shift_days (
  date integer not null,
  user_id uuid not null,
  shift_type text not null,
  day_status text not null,
  note text null,
  updated_at bigint not null,
  primary key (date, user_id)
);

alter table public.planwise_events enable row level security;
alter table public.planwise_shift_days enable row level security;

create policy "planwise_events_owner_rw"
on public.planwise_events
for all
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

create policy "planwise_shift_days_owner_rw"
on public.planwise_shift_days
for all
using (auth.uid() = user_id)
with check (auth.uid() = user_id);
